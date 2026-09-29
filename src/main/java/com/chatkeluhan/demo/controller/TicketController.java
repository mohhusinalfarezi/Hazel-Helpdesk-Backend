package com.chatkeluhan.demo.controller;

import com.chatkeluhan.demo.entity.Asset;
import com.chatkeluhan.demo.entity.ProblemCategory;
import com.chatkeluhan.demo.entity.Ticket;
import com.chatkeluhan.demo.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.chatkeluhan.demo.dto.ticket.TicketRequest;
import com.chatkeluhan.demo.dto.ticket.TicketResponse;
import com.chatkeluhan.demo.dto.ticket.TicketStatusUpdateRequest;
import org.springframework.security.core.Authentication;

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

    // --- API ENDPOINTS ---

    // POST: Membuat tiket baru (JWT Authenticated)
    @PostMapping
    public ResponseEntity<TicketResponse> createTicketApi(
            @RequestBody TicketRequest request,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        try {
            TicketResponse response = ticketService.createTicketApi(userEmail, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET: Mengambil semua tiket milik user yang sedang login (JWT Authenticated)
    @GetMapping
    public ResponseEntity<List<TicketResponse>> getMyTicketsApi(Authentication authentication) {
        String userEmail = authentication.getName();
        try {
            List<TicketResponse> responses = ticketService.getMyTicketsApi(userEmail);
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PATCH: Mengupdate status tiket
    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<TicketResponse> updateTicketStatusApi(
            @PathVariable String ticketId,
            @RequestBody TicketStatusUpdateRequest request) {
        
        try {
            TicketResponse response = ticketService.updateTicketStatusApi(ticketId, request.getStatus());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}