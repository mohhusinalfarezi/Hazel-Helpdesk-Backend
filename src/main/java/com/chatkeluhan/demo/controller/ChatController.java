package com.chatkeluhan.demo.controller;

import com.chatkeluhan.demo.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // Sangat krusial: Membuka gerbang agar aplikasi Flutter (emulator/HP fisik)
                            // diizinkan mengakses API ini
public class ChatController {

    private final GeminiService geminiService;

    // Dependency Injection
    public ChatController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> chatWithHazel(@RequestBody Map<String, String> request) {
        // 1. Menangkap pesan dari Flutter
        String userMessage = request.get("message");

        // Validasi input kosong
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Pesan tidak boleh kosong"));
        }

        try {
            // 2. Menyerahkan pesan ke Hazel (GeminiService) yang sudah kita buat
            String aiResponse = geminiService.getAiResponse(userMessage);

            // 3. Mengemas balasan Hazel menjadi format JSON untuk Flutter (key "reply"
            // harus cocok dengan Flutter)
            return ResponseEntity.ok(Map.of("reply", aiResponse));

        } catch (Exception e) {
            System.err.println("[CONTROLLER ERROR] Terjadi kesalahan saat memanggil Gemini: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Terjadi kesalahan internal pada server AI."));
        }
    }
}