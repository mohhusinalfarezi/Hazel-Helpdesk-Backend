package com.chatkeluhan.demo.controller;

import com.chatkeluhan.demo.entity.Announcement;
import com.chatkeluhan.demo.entity.Customer;
import com.chatkeluhan.demo.entity.UserEntity;
import com.chatkeluhan.demo.repository.CustomerRepository;
import com.chatkeluhan.demo.repository.UserRepository;
import com.chatkeluhan.demo.service.AnnouncementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public AnnouncementController(AnnouncementService announcementService,
                                  CustomerRepository customerRepository,
                                  UserRepository userRepository) {
        this.announcementService = announcementService;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    /**
     * GET /api/announcements
     * Mengembalikan semua pengumuman (terurut dari yang terbaru).
     */
    @GetMapping
    public ResponseEntity<List<Announcement>> getAllAnnouncements() {
        return ResponseEntity.ok(announcementService.getAllAnnouncements());
    }

    /**
     * POST /api/announcements
     * Menerima JSON Announcement, set authorName dari user/teknisi yang login, simpan, kembalikan 200 OK.
     */
    @PostMapping
    public ResponseEntity<?> createAnnouncement(@RequestBody Announcement announcement,
                                               Authentication authentication) {
        try {
            String authorName = null;
            if (authentication != null && authentication.getName() != null) {
                String email = authentication.getName();

                // Cari dari Customer (teknisi / user yang login via AuthService)
                Customer customer = customerRepository.findByEmail(email).orElse(null);
                if (customer != null && customer.getFullName() != null && !customer.getFullName().isBlank()) {
                    authorName = customer.getFullName();
                } else {
                    // Cari dari UserEntity jika ada
                    UserEntity user = userRepository.findByEmail(email).orElse(null);
                    if (user != null && user.getName() != null && !user.getName().isBlank()) {
                        authorName = user.getName();
                    } else {
                        authorName = email;
                    }
                }
            }

            if (authorName != null && !authorName.isBlank()) {
                announcement.setAuthorName(authorName);
            } else if (announcement.getAuthorName() == null || announcement.getAuthorName().isBlank()) {
                announcement.setAuthorName("Admin");
            }

            Announcement created = announcementService.createAnnouncement(announcement);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/announcements/{id}
     * Menghapus pengumuman berdasarkan ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnnouncement(@PathVariable Long id) {
        try {
            announcementService.deleteAnnouncement(id);
            return ResponseEntity.ok(Map.of("message", "Pengumuman berhasil dihapus"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
