package com.chatkeluhan.demo.controller;

import com.chatkeluhan.demo.entity.Customer;
import com.chatkeluhan.demo.entity.KnowledgeArticle;
import com.chatkeluhan.demo.entity.UserEntity;
import com.chatkeluhan.demo.repository.CustomerRepository;
import com.chatkeluhan.demo.repository.UserRepository;
import com.chatkeluhan.demo.service.KnowledgeArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge-articles")
public class KnowledgeArticleController {

    private final KnowledgeArticleService knowledgeArticleService;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public KnowledgeArticleController(KnowledgeArticleService knowledgeArticleService,
                                      CustomerRepository customerRepository,
                                      UserRepository userRepository) {
        this.knowledgeArticleService = knowledgeArticleService;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    /**
     * GET /api/knowledge-articles
     * Mengembalikan daftar artikel basis pengetahuan.
     * Opsional: parameter ?category=Jaringan untuk memfilter per kategori.
     */
    @GetMapping
    public ResponseEntity<List<KnowledgeArticle>> getArticles(
            @RequestParam(required = false) String category) {
        if (category != null && !category.trim().isEmpty()) {
            return ResponseEntity.ok(knowledgeArticleService.getArticlesByCategory(category));
        }
        return ResponseEntity.ok(knowledgeArticleService.getAllArticles());
    }

    /**
     * GET /api/knowledge-articles/{id}
     * Mengembalikan detail artikel berdasarkan ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getArticleById(@PathVariable Long id) {
        return knowledgeArticleService.getArticleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/knowledge-articles
     * Menyimpan artikel baru dengan authorName dari akun yang login.
     */
    @PostMapping
    public ResponseEntity<?> createArticle(@RequestBody KnowledgeArticle article,
                                          Authentication authentication) {
        try {
            String authorName = null;
            if (authentication != null && authentication.getName() != null) {
                String email = authentication.getName();

                // Cari dari Customer (pengguna / teknisi yang login via auth service)
                Customer customer = customerRepository.findByEmail(email).orElse(null);
                if (customer != null && customer.getFullName() != null && !customer.getFullName().isBlank()) {
                    authorName = customer.getFullName();
                } else {
                    // Cari dari UserEntity
                    UserEntity user = userRepository.findByEmail(email).orElse(null);
                    if (user != null && user.getName() != null && !user.getName().isBlank()) {
                        authorName = user.getName();
                    } else {
                        authorName = email;
                    }
                }
            }

            if (authorName != null && !authorName.isBlank()) {
                article.setAuthorName(authorName);
            } else if (article.getAuthorName() == null || article.getAuthorName().isBlank()) {
                article.setAuthorName("Tim Support IT");
            }

            KnowledgeArticle created = knowledgeArticleService.createArticle(article);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/knowledge-articles/{id}
     * Menghapus artikel berdasarkan ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticle(@PathVariable Long id) {
        try {
            knowledgeArticleService.deleteArticle(id);
            return ResponseEntity.ok(Map.of("message", "Artikel basis pengetahuan berhasil dihapus"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
