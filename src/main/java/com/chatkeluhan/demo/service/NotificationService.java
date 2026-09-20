package com.chatkeluhan.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class NotificationService {

    // Default URL menggunakan Fonnte sebagai contoh kerangka
    @Value("${whatsapp.api.url:https://api.fonnte.com/send}")
    private String apiUrl;

    @Value("${whatsapp.api.token:TOKEN_SEMENTARA_BELUM_DISET}")
    private String apiToken;

    private final RestTemplate restTemplate;

    public NotificationService() {
        this.restTemplate = new RestTemplate();
    }

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