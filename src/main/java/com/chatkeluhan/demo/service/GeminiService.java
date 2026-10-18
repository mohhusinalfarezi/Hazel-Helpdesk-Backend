package com.chatkeluhan.demo.service;

import com.chatkeluhan.demo.dto.ChatResponseDto;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeminiService {

    private static final Pattern ESCALATE_PATTERN = Pattern.compile("<<<ESCALATE:\\s*(\\{.*?\\})\\s*>>>", Pattern.DOTALL);

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
        return getAiChatResponse(userMessage, ticket).getReply();
    }

    public ChatResponseDto getAiChatResponse(String userMessage, com.chatkeluhan.demo.entity.Ticket ticket) {
        chatHistory.add(Map.of("role", "user", "parts", new Object[]{ Map.of("text", userMessage) }));

        int maxRetries = 3;
        long backoffTime = 2000; 

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String rawUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=" + apiKey;
                URI uri = new URI(rawUrl);

                String baseInstruction = "Kamu adalah Asisten AI Helpdesk dari sebuah perusahaan BUMN bernama Hazel.\n" +
                        "Karakter dan Sikap:\n" +
                        "- Bersikaplah ramah, hangat, penuh empati, solutif, dan tidak kaku.\n" +
                        "- Berikan langkah perbaikan IT yang ringkas, jelas, dan mudah diikuti oleh pengguna.\n\n" +
                        "Panduan Operasional:\n" +
                        "1. DIAGNOSA & TROUBLESHOOTING:\n" +
                        "   - Berikan solusi atau langkah perbaikan mandiri terlebih dahulu secara bertahap dan ringkas.\n" +
                        "2. DETEKSI KEBUTUHAN ESKALASI TIKET:\n" +
                        "   - Jika masalah pengguna terdeteksi tidak bisa diselesaikan secara mandiri,\n" +
                        "   - ATAU pengguna menyatakan kendala masih berlanjut (misal: 'tetap tidak bisa', 'masih mati', 'sudah dicoba tapi gagal'),\n" +
                        "   - ATAU masalah membutuhkan kehadiran/penanganan teknisi fisik langsung,\n" +
                        "   MAKA: Tetap berikan jawaban yang menenangkan bahwa kendala mereka dipahami dan akan dieskalasikan ke tim teknisi.\n" +
                        "   Sertakan tag khusus di akhir balasanmu dengan format persis:\n" +
                        "   <<<ESCALATE:{\"needTicket\":true,\"category\":\"Network|Hardware|Software|General\",\"summary\":\"ringkasan kendala singkat\"}>>>\n" +
                        "   (Pilih salah satu category yang paling relevan: Network, Hardware, Software, atau General. Pada summary, berikan ringkasan kendala singkat).\n" +
                        "   - Jika masalah sudah selesai atau masih dalam tahap diagnosa/troubleshooting awal, JANGAN sertakan tag <<<ESCALATE:...>>> tersebut.\n\n" +
                        "3. CEK STATUS TIKET:\n" +
                        "   - Jika pelanggan ingin mengecek status atau mem-follow-up laporan, tanyakan Nomor Tiketnya (misal: TKT-123). " +
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
                
                // --- LOGIKA 1: INTERSEPSI PEMBUATAN TIKET (LEGACY / CHATBOT BOT) ---
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

                // --- LOGIKA 3: DETEKSI & EKSTRAKSI ESKALASI TIKET <<<ESCALATE:...>>> ---
                boolean shouldEscalate = false;
                String suggestedCategory = null;
                String suggestedSummary = null;

                Matcher matcher = ESCALATE_PATTERN.matcher(displayReply);
                if (matcher.find()) {
                    String escalateJson = matcher.group(1).trim();
                    try {
                        JsonNode escalateNode = objectMapper.readTree(escalateJson);
                        shouldEscalate = escalateNode.path("needTicket").asBoolean(true);
                        if (escalateNode.hasNonNull("category")) {
                            suggestedCategory = escalateNode.path("category").asText();
                        }
                        if (escalateNode.hasNonNull("summary")) {
                            suggestedSummary = escalateNode.path("summary").asText();
                        }
                    } catch (Exception ex) {
                        System.err.println("[LOG AI] Gagal parse JSON eskalasi: " + ex.getMessage());
                        shouldEscalate = true;
                        suggestedCategory = "General";
                    }

                    // Bersihkan tag eskalasi dari teks balasan yang akan dibaca pengguna
                    displayReply = matcher.replaceAll("").trim();
                }
                
                // Simpan hanya teks AI yang bersih ke dalam memori percakapan
                chatHistory.add(Map.of("role", "model", "parts", new Object[]{ Map.of("text", displayReply) }));
                
                if (ticket != null && ticketMessageRepository != null) {
                    com.chatkeluhan.demo.entity.TicketMessage userMsgEntity = new com.chatkeluhan.demo.entity.TicketMessage();
                    userMsgEntity.setTicket(ticket);
                    userMsgEntity.setSender("USER");
                    userMsgEntity.setSenderRole("USER");
                    userMsgEntity.setSenderEmail("user@system.local");
                    userMsgEntity.setSenderName("User");
                    userMsgEntity.setMessage(userMessage);
                    ticketMessageRepository.save(userMsgEntity);

                    com.chatkeluhan.demo.entity.TicketMessage aiMsgEntity = new com.chatkeluhan.demo.entity.TicketMessage();
                    aiMsgEntity.setTicket(ticket);
                    aiMsgEntity.setSender("AI");
                    aiMsgEntity.setSenderRole("AI");
                    aiMsgEntity.setSenderEmail("hazel@helpdesk.ai");
                    aiMsgEntity.setSenderName("Hazel AI");
                    aiMsgEntity.setMessage(displayReply);
                    ticketMessageRepository.save(aiMsgEntity);
                }

                return new ChatResponseDto(displayReply, shouldEscalate, suggestedCategory, suggestedSummary);

            } catch (org.springframework.web.client.HttpStatusCodeException e) {
                if (e.getStatusCode().value() == 503 && attempt < maxRetries) {
                    System.out.println("Mendapat error 503, mencoba lagi dalam " + (backoffTime / 1000) + " detik...");
                    try { Thread.sleep(backoffTime); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
                    backoffTime *= 2; 
                } else if (e.getStatusCode().value() == 503) {
                    chatHistory.remove(chatHistory.size() - 1);
                    return new ChatResponseDto("Maaf, Hazel sedang sangat sibuk saat ini. Mohon coba lagi beberapa saat kemudian.", false, null, null);
                } else if (e.getStatusCode().value() == 429) {
                    chatHistory.remove(chatHistory.size() - 1);
                    return new ChatResponseDto("Maaf, sistem layanan AI kami sedang mencapai batas limit penggunaan (Error 429). Mohon tunggu beberapa saat sebelum mencoba kembali.", false, null, null);
                } else {
                    chatHistory.remove(chatHistory.size() - 1);
                    return new ChatResponseDto("Maaf, Hazel sedang mengalami gangguan koneksi HTTP: " + e.getMessage(), false, null, null);
                }
            } catch (Exception e) {
                chatHistory.remove(chatHistory.size() - 1);
                return new ChatResponseDto("Maaf, Hazel sedang mengalami gangguan eksekusi: " + e.getMessage(), false, null, null);
            }
        }
        
        chatHistory.remove(chatHistory.size() - 1);
        return new ChatResponseDto("Maaf, Hazel gagal merespons setelah beberapa kali percobaan.", false, null, null);
    }
}