package com.chatkeluhan.demo.controller;

import com.chatkeluhan.demo.dto.ChatResponseDto;
import com.chatkeluhan.demo.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // Membuka gerbang agar aplikasi klien (Flutter/Web) diizinkan mengakses API ini
public class ChatController {

    private final GeminiService geminiService;
    private final com.chatkeluhan.demo.repository.TicketRepository ticketRepository;

    public ChatController(GeminiService geminiService, com.chatkeluhan.demo.repository.TicketRepository ticketRepository) {
        this.geminiService = geminiService;
        this.ticketRepository = ticketRepository;
    }

    @PostMapping
    public ResponseEntity<?> chatWithHazel(@RequestBody Map<String, String> request) {
        // 1. Menangkap pesan dari request
        String userMessage = request.get("message");
        String ticketId = request.get("ticketId");

        // Validasi input kosong
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Pesan tidak boleh kosong"));
        }

        try {
            com.chatkeluhan.demo.entity.Ticket ticket = null;
            if (ticketId != null && !ticketId.trim().isEmpty()) {
                ticket = ticketRepository.findById(ticketId).orElse(null);
            }

            // 2. Menyerahkan pesan ke Hazel AI dan mengekstrak respons serta metadata eskalasi
            ChatResponseDto responseDto = geminiService.getAiChatResponse(userMessage, ticket);

            // 3. Mengembalikan response terstruktur: reply, shouldEscalate, suggestedCategory, suggestedSummary
            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            System.err.println("[CONTROLLER ERROR] Terjadi kesalahan saat memanggil Gemini: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Terjadi kesalahan internal pada server AI."));
        }
    }
}