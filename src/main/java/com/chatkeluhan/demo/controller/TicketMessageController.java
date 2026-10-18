package com.chatkeluhan.demo.controller;

import com.chatkeluhan.demo.entity.TicketMessage;
import com.chatkeluhan.demo.entity.UserEntity;
import com.chatkeluhan.demo.repository.UserRepository;
import com.chatkeluhan.demo.service.TicketMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets/{ticketId}/messages")
public class TicketMessageController {

    private final TicketMessageService ticketMessageService;
    private final UserRepository userRepository;

    public TicketMessageController(TicketMessageService ticketMessageService,
                                   UserRepository userRepository) {
        this.ticketMessageService = ticketMessageService;
        this.userRepository = userRepository;
    }

    /**
     * GET /api/tickets/{ticketId}/messages
     * Mengembalikan semua pesan obrolan untuk tiket tertentu, terurut dari terlama ke terbaru.
     */
    @GetMapping
    public ResponseEntity<List<TicketMessage>> getMessages(@PathVariable String ticketId) {
        List<TicketMessage> messages = ticketMessageService.getMessagesByTicketId(ticketId);
        return ResponseEntity.ok(messages);
    }

    /**
     * POST /api/tickets/{ticketId}/messages
     * Menerima payload body {"message": "..."}, mengambil user dari JWT, menyimpan pesan,
     * dan mengembalikan pesan yang tersimpan.
     */
    @PostMapping
    public ResponseEntity<?> sendMessage(@PathVariable String ticketId,
                                         @RequestBody Map<String, String> payload,
                                         Authentication authentication) {
        try {
            String messageText = payload.get("message");
            if (messageText == null || messageText.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Pesan tidak boleh kosong."));
            }

            // Ambil email pengirim dari JWT (subject = email)
            String senderEmail = authentication.getName();

            // Tentukan role dari authorities JWT
            String senderRole = "USER"; // default
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String auth = authority.getAuthority();
                if (auth.contains("TECHNICIAN") || auth.contains("ADMIN")) {
                    senderRole = "TECHNICIAN";
                    break;
                }
            }

            // Ambil nama pengirim dari database
            String senderName = senderEmail; // fallback ke email
            UserEntity user = userRepository.findByEmail(senderEmail).orElse(null);
            if (user != null && user.getName() != null && !user.getName().isBlank()) {
                senderName = user.getName();
            }

            TicketMessage savedMessage = ticketMessageService.sendMessage(
                    ticketId, senderEmail, senderName, senderRole, messageText.trim());

            return ResponseEntity.ok(savedMessage);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
