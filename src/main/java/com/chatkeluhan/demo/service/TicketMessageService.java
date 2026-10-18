package com.chatkeluhan.demo.service;

import com.chatkeluhan.demo.entity.Ticket;
import com.chatkeluhan.demo.entity.TicketMessage;
import com.chatkeluhan.demo.repository.TicketMessageRepository;
import com.chatkeluhan.demo.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketMessageService {

    private final TicketMessageRepository ticketMessageRepository;
    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;

    public TicketMessageService(TicketMessageRepository ticketMessageRepository,
                                TicketRepository ticketRepository,
                                NotificationService notificationService) {
        this.ticketMessageRepository = ticketMessageRepository;
        this.ticketRepository = ticketRepository;
        this.notificationService = notificationService;
    }

    /**
     * Mengembalikan daftar pesan obrolan tiket terurut dari yang terlama ke terbaru.
     */
    public List<TicketMessage> getMessagesByTicketId(String ticketId) {
        return ticketMessageRepository.findByTicket_TicketIdOrderByCreatedAtAsc(ticketId);
    }

    /**
     * Menyimpan pesan obrolan baru ke database dan mengirim notifikasi in-app.
     *
     * Jika pengirim adalah USER   -> notifikasi dikirim ke teknisi yang menangani (atau admin jika belum ada teknisi).
     * Jika pengirim adalah TECHNICIAN -> notifikasi dikirim ke email pemilik tiket (customer).
     */
    public TicketMessage sendMessage(String ticketId, String senderEmail, String senderName,
                                     String senderRole, String messageText) {

        // 1. Cari tiket terkait
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Tiket dengan ID " + ticketId + " tidak ditemukan."));

        // 2. Simpan pesan ke database
        TicketMessage ticketMessage = new TicketMessage();
        ticketMessage.setTicket(ticket);
        ticketMessage.setSender(senderEmail);
        ticketMessage.setSenderEmail(senderEmail);
        ticketMessage.setSenderName(senderName);
        ticketMessage.setSenderRole(senderRole);
        ticketMessage.setMessage(messageText);

        TicketMessage savedMessage = ticketMessageRepository.save(ticketMessage);

        // 3. Kirim notifikasi berdasarkan role pengirim
        if ("USER".equalsIgnoreCase(senderRole)) {
            // Pengirim adalah USER -> kirim notifikasi ke teknisi yang menangani
            String recipientEmail;
            if (ticket.getTechnician() != null) {
                // Gunakan technicianId sebagai identifier (bisa jadi email atau ID tergantung implementasi)
                recipientEmail = ticket.getTechnician().getTechnicianId();
            } else {
                // Jika belum ada teknisi yang ditugaskan, kirim ke admin default
                recipientEmail = "admin@helpdesk.com";
            }

            notificationService.createNotification(
                    recipientEmail,
                    "Pesan Baru dari Pelanggan",
                    senderName + " mengirim pesan pada tiket " + ticketId + ": " + truncateMessage(messageText),
                    "CHAT_MESSAGE",
                    String.valueOf(ticketId)
            );

        } else if ("TECHNICIAN".equalsIgnoreCase(senderRole)) {
            // Pengirim adalah TECHNICIAN -> kirim notifikasi ke pemilik tiket (customer)
            String customerEmail = ticket.getCustomer().getEmail();

            notificationService.createNotification(
                    customerEmail,
                    "Pesan Baru dari Teknisi",
                    senderName + " membalas pesan pada tiket " + ticketId + ": " + truncateMessage(messageText),
                    "CHAT_MESSAGE",
                    String.valueOf(ticketId)
            );
        }

        return savedMessage;
    }

    /**
     * Memotong pesan agar notifikasi tidak terlalu panjang.
     */
    private String truncateMessage(String message) {
        if (message != null && message.length() > 100) {
            return message.substring(0, 100) + "...";
        }
        return message;
    }
}
