package com.chatkeluhan.demo.service;

import com.chatkeluhan.demo.entity.Notification;
import com.chatkeluhan.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    // Default URL menggunakan Fonnte sebagai contoh kerangka
    @Value("${whatsapp.api.url:https://api.fonnte.com/send}")
    private String apiUrl;

    @Value("${whatsapp.api.token:TOKEN_SEMENTARA_BELUM_DISET}")
    private String apiToken;

    private final RestTemplate restTemplate;
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.restTemplate = new RestTemplate();
        this.notificationRepository = notificationRepository;
    }

    // =============================================
    // === FITUR NOTIFIKASI TERPUSAT (IN-APP) ===
    // =============================================

    /**
     * Membuat dan menyimpan notifikasi baru.
     */
    public Notification createNotification(String recipientEmail, String title, String message, String type, String relatedId) {
        Notification notification = new Notification();
        notification.setRecipientEmail(recipientEmail);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        return notificationRepository.save(notification);
    }

    /**
     * Mengambil semua notifikasi milik user berdasarkan email, diurutkan dari terbaru.
     */
    public List<Notification> getUserNotifications(String email) {
        return notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);
    }

    /**
     * Menandai notifikasi sebagai telah dibaca.
     */
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notifikasi dengan ID " + notificationId + " tidak ditemukan."));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    // =============================================
    // === FITUR NOTIFIKASI WHATSAPP (LEGACY) ===
    // =============================================

    public void sendWhatsAppMessage(String noHp, String ticketId, String masalah) {
        try {
            // Merakit isi pesan WhatsApp yang rapi
            String pesan = "*HELPDESK BUMN - LAPORAN DITERIMA*\n\n" +
                           "Halo, laporan Anda telah kami terima dan masuk ke dalam sistem dengan detail berikut:\n" +
                           "- Nomor Tiket: *" + ticketId + "*\n" +
                           "- Kendala: " + masalah + "\n\n" +
                           "Tim teknis darurat kami akan segera menindaklanjuti laporan ini. " +
                           "Anda dapat mengecek status penanganan kapan saja dengan membalas chat ini menggunakan Nomor Tiket Anda.\n\n" +
                           "Terima kasih atas kepedulian Anda!";

            // Konfigurasi Header untuk Otentikasi API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", apiToken);

            // Merakit Body (Struktur JSON menyesuaikan penyedia API, ini contoh standar target & message)
            Map<String, String> requestBody = Map.of(
                "target", noHp,
                "message", pesan
            );

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            // Mengirim request ke Server WhatsApp Gateway
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            System.out.println("\n[LOG NOTIFIKASI] Perintah kirim WhatsApp dieksekusi untuk: " + noHp);
            System.out.println("[LOG NOTIFIKASI] Respons dari WhatsApp Gateway: " + response.getBody());
        } catch (Exception e) {
            // Sengaja dibungkus try-catch agar jika WhatsApp gagal, aplikasi chat AI tidak ikut mati
            System.err.println("\n[SISTEM ERROR] Gagal mengirim notifikasi WhatsApp: " + e.getMessage());
        }
    }
}