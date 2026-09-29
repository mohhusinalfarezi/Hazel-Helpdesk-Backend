package com.chatkeluhan.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final List<Map<String, Object>> chatHistory = new ArrayList<>();
    private final TicketService ticketService;
    private final com.chatkeluhan.demo.repository.TicketMessageRepository ticketMessageRepository;

    public GeminiService(TicketService ticketService, com.chatkeluhan.demo.repository.TicketMessageRepository ticketMessageRepository) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.ticketService = ticketService;
        this.ticketMessageRepository = ticketMessageRepository;
    }

    public String getAiResponse(String userMessage) {
        return getAiResponse(userMessage, null);
    }

    public String getAiResponse(String userMessage, com.chatkeluhan.demo.entity.Ticket ticket) {
        chatHistory.add(Map.of("role", "user", "parts", new Object[]{ Map.of("text", userMessage) }));

        int maxRetries = 3;
        long backoffTime = 2000; 

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String rawUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + apiKey;
                URI uri = new URI(rawUrl);

                String baseInstruction = "Kamu adalah Asisten AI Helpdesk dari sebuah perusahaan BUMN bernama Hazel. " +
                        "Gaya bahasamu ramah, hangat, empati, dan tidak kaku. " +
                        "Tugasmu ada DUA:\n" +
                        "1. MEMBUAT TIKET: Jika pelanggan melapor masalah, tanyakan lokasi spesifik dan nomor HP. " +
                        "JIKA lengkap, berikan penutup ramah, beritahu Nomor Tiket akan ditampilkan sistem di layar ini dan salinannya dikirim via WhatsApp. " +
                        "LALU WAJIB tambahkan kode [TICKET_READY: {\"no_hp\":\"...\", \"lokasi\":\"...\", \"masalah\":\"...\"}] di baris paling bawah.\n\n" +
                        "2. CEK STATUS: Jika pelanggan ingin mengecek status atau mem-follow-up laporan, tanyakan Nomor Tiketnya (misal: TKT-123). " +
                        "Jika pelanggan sudah menyebutkan nomor tiket, berikan respons bahwa sistem sedang mengeceknya, " +
                        "LALU WAJIB tambahkan kode [TICKET_CHECK: {\"ticket_id\":\"...\"}] di baris paling bawah.";

                if (ticket != null) {
                    baseInstruction += "\n\nINFORMASI TIKET SAAT INI (Konteks Pengguna):\n" +
                            "- ID Tiket: " + ticket.getTicketId() + "\n" +
                            "- Deskripsi Keluhan: " + ticket.getDescription() + "\n" +
                            "- Status Saat Ini: " + ticket.getCurrentStatus() + "\n" +
                            "- Kategori: " + (ticket.getCategory() != null ? ticket.getCategory().getCategoryName() : "Umum") + "\n" +
                            "Gunakan informasi di atas untuk merespons pertanyaan pengguna yang berkaitan dengan tiket ini secara spesifik.";
                }

                Map<String, Object> systemInstruction = Map.of(
                    "parts", new Object[]{ Map.of("text", baseInstruction) }
                );

                Map<String, Object> requestBody = Map.of(
                    "systemInstruction", systemInstruction,
                    "contents", chatHistory
                );

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(uri, entity, String.class);
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                
                String aiReply = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
                String displayReply = aiReply;
                
                // --- LOGIKA 1: INTERSEPSI PEMBUATAN TIKET ---
                if (aiReply.contains("[TICKET_READY:")) {
                    try {
                        int startIndex = aiReply.indexOf("[TICKET_READY:");
                        String teksRamahAi = aiReply.substring(0, startIndex).trim();
                        
                        int jsonStartIndex = startIndex + 14; 
                        int jsonEndIndex = aiReply.lastIndexOf("]");
                        
                        if (jsonStartIndex != -1 && jsonEndIndex != -1 && jsonStartIndex < jsonEndIndex) {
                            String jsonPart = aiReply.substring(jsonStartIndex, jsonEndIndex).trim();
                            JsonNode ticketData = objectMapper.readTree(jsonPart);
                            String noHp = ticketData.path("no_hp").asText();
                            String lokasi = ticketData.path("lokasi").asText();
                            String masalah = ticketData.path("masalah").asText();
                            
                            String nomorTiket = ticketService.createTicketFromExtractedData(noHp, lokasi, masalah);
                            
                            String systemNotification = "\n\n=========================================\n" +
                                                        "[SISTEM INFO] Laporan Berhasil Disimpan\n" +
                                                        "NOMOR TIKET : " + nomorTiket + "\n" +
                                                        "(Salinan otomatis telah dikirim via WhatsApp ke " + noHp + ")\n" +
                                                        "=========================================";
                            
                            displayReply = teksRamahAi + systemNotification;
                            System.out.println("\n[LOG BACKEND] Tiket sukses dibuat dengan ID: " + nomorTiket);
                        }
                    } catch (Exception ex) {
                        System.err.println("\n[SISTEM ERROR] Gagal mem-parsing data tiket: " + ex.getMessage());
                    }
                } 
                // --- LOGIKA 2: INTERSEPSI CEK STATUS TIKET ---
                else if (aiReply.contains("[TICKET_CHECK:")) {
                    try {
                        int startIndex = aiReply.indexOf("[TICKET_CHECK:");
                        String teksRamahAi = aiReply.substring(0, startIndex).trim();
                        
                        int jsonStartIndex = startIndex + 14; 
                        int jsonEndIndex = aiReply.lastIndexOf("]");
                        
                        if (jsonStartIndex != -1 && jsonEndIndex != -1 && jsonStartIndex < jsonEndIndex) {
                            String jsonPart = aiReply.substring(jsonStartIndex, jsonEndIndex).trim();
                            JsonNode checkData = objectMapper.readTree(jsonPart);
                            String ticketId = checkData.path("ticket_id").asText();
                            
                            // Query Database via TicketService
                            String statusResult = ticketService.checkTicketStatus(ticketId);
                            
                            String systemNotification = "\n\n=========================================\n" +
                                                        "[SISTEM INFO] Hasil Pengecekan Tiket\n" +
                                                        "NOMOR TIKET : " + ticketId + "\n" +
                                                        "HASIL       : " + statusResult + "\n" +
                                                        "=========================================";
                            
                            displayReply = teksRamahAi + systemNotification;
                            System.out.println("\n[LOG BACKEND] Permintaan cek status untuk tiket: " + ticketId);
                        }
                    } catch (Exception ex) {
                        System.err.println("\n[SISTEM ERROR] Gagal mengekstrak data pengecekan tiket: " + ex.getMessage());
                    }
                }
                
                // Simpan hanya teks AI yang sudah digabung (tanpa JSON rahasia) ke dalam memori
                chatHistory.add(Map.of("role", "model", "parts", new Object[]{ Map.of("text", displayReply) }));
                
                if (ticket != null && ticketMessageRepository != null) {
                    com.chatkeluhan.demo.entity.TicketMessage userMsgEntity = new com.chatkeluhan.demo.entity.TicketMessage();
                    userMsgEntity.setTicket(ticket);
                    userMsgEntity.setSender("USER");
                    userMsgEntity.setMessageText(userMessage);
                    userMsgEntity.setSentAt(java.time.LocalDateTime.now());
                    ticketMessageRepository.save(userMsgEntity);

                    com.chatkeluhan.demo.entity.TicketMessage aiMsgEntity = new com.chatkeluhan.demo.entity.TicketMessage();
                    aiMsgEntity.setTicket(ticket);
                    aiMsgEntity.setSender("AI");
                    aiMsgEntity.setMessageText(displayReply);
                    aiMsgEntity.setSentAt(java.time.LocalDateTime.now());
                    ticketMessageRepository.save(aiMsgEntity);
                }

                return displayReply;

            } catch (org.springframework.web.client.HttpStatusCodeException e) {
                if (e.getStatusCode().value() == 503 && attempt < maxRetries) {
                    System.out.println("Mendapat error 503, mencoba lagi dalam " + (backoffTime / 1000) + " detik...");
                    try { Thread.sleep(backoffTime); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
                    backoffTime *= 2; 
                } else if (e.getStatusCode().value() == 503) {
                    chatHistory.remove(chatHistory.size() - 1);
                    return "Maaf, Hazel sedang sangat sibuk saat ini. Mohon coba lagi beberapa saat kemudian.";
                } else if (e.getStatusCode().value() == 429) {
                    // --- PENANGANAN ERROR 429 (QUOTA HABIS) DITAMBAHKAN DI SINI ---
                    chatHistory.remove(chatHistory.size() - 1);
                    return "Maaf, sistem layanan AI kami sedang mencapai batas limit penggunaan (Error 429). Mohon tunggu beberapa saat sebelum mencoba kembali.";
                } else {
                    chatHistory.remove(chatHistory.size() - 1);
                    return "Maaf, Hazel sedang mengalami gangguan koneksi HTTP: " + e.getMessage();
                }
            } catch (Exception e) {
                chatHistory.remove(chatHistory.size() - 1);
                return "Maaf, Hazel sedang mengalami gangguan eksekusi: " + e.getMessage();
            }
        }
        
        chatHistory.remove(chatHistory.size() - 1);
        return "Maaf, Hazel gagal merespons setelah beberapa kali percobaan.";
    }
}