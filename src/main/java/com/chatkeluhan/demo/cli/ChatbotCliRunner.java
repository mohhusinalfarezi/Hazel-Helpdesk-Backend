package com.chatkeluhan.demo.cli;

import com.chatkeluhan.demo.service.GeminiService;
import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;

import java.util.Scanner;

// @Component // <-- Anotasi ini dimatikan agar Spring Boot tidak menjalankan file ini secara otomatis
public class ChatbotCliRunner implements CommandLineRunner {

    private final GeminiService geminiService;

    // Dependency Injection
    public ChatbotCliRunner(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("[INFO] Mode Terminal dinonaktifkan. Mengalihkan ke mode REST API Server...");

        /*
         * ===== SEMUA KODE DI BAWAH INI DIMATIKAN AGAR TIDAK MEMBLOKIR TOMCAT =====
         * 
         * Scanner scanner = new Scanner(System.in);
         * 
         * System.out.println(
         * "\n=========================================================");
         * System.out.println("  Hazel Helpdesk BUMN (Mode Terminal Aktif)  ");
         * System.out.println("  Ketik 'keluar' untuk menghentikan obrolan.     ");
         * System.out.println(
         * "=========================================================\n");
         * 
         * while (true) {
         * System.out.print("Kamu: ");
         * String userInput = scanner.nextLine();
         * 
         * if (userInput.equalsIgnoreCase("keluar")) {
         * System.out.
         * println("Hazel: Mematikan sesi obrolan. Sistem API tetap berjalan.");
         * break;
         * }
         * 
         * System.out.println("Hazel: [Sedang berpikir...]");
         * 
         * // Mengirimkan input ke server Google Gemini
         * String aiResponse = geminiService.getAiResponse(userInput);
         * 
         * // Mencetak balasan dari Gemini ke terminal
         * System.out.println("Hazel: " + aiResponse + "\n");
         * }
         * 
         * ==========================================================================
         */
    }
}