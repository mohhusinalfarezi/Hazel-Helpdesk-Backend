package com.chatkeluhan.demo.controller;

import com.chatkeluhan.demo.entity.Asset;
import com.chatkeluhan.demo.entity.ProblemCategory;
import com.chatkeluhan.demo.entity.Ticket;
import com.chatkeluhan.demo.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTicket(@RequestBody Map<String, String> payload) {
        try {
            // Mengekstrak data yang dikirim oleh Chatbot dalam format JSON
            String contactNumber = payload.get("contactNumber");
            String description = payload.get("description");
            
            // Memetakan ID referensi dari Chatbot ke dalam wujud Entitas
            Asset asset = new Asset();
            asset.setAssetId(payload.get("assetId")); 

            ProblemCategory category = new ProblemCategory();
            category.setCategoryId(payload.get("categoryId"));

            // Menjalankan logika bisnis penyimpan tiket
            Ticket newTicket = ticketService.createTicketFromBot(contactNumber, asset, category, description);
            
            // Mengembalikan respons sukses ke Chatbot
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Tiket keluhan berhasil diterbitkan.",
                "ticketId", newTicket.getTicketId()
            ));
        } catch (Exception e) {
            // Menangkap error (seperti nomor tidak terdaftar) dan mengirimkannya kembali ke Chatbot
            return ResponseEntity.badRequest().body(Map.of(
                "status", "failed",
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/history/{contactNumber}")
    public ResponseEntity<?> getCustomerHistory(@PathVariable String contactNumber) {
        try {
            List<Ticket> history = ticketService.getTicketsByContactNumber(contactNumber);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "totalTickets", history.size(),
                "data", history
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "failed", "error", e.getMessage()));
        }
    }
}