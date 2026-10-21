package com.chatkeluhan.demo.controller;

import com.chatkeluhan.demo.entity.ServiceCatalog;
import com.chatkeluhan.demo.service.ServiceCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/service-catalogs")
public class ServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceCatalogController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    /**
     * GET /api/service-catalogs
     * Mengembalikan daftar katalog layanan aktif (dapat diakses oleh seluruh pengguna terotentikasi).
     */
    @GetMapping
    public ResponseEntity<List<ServiceCatalog>> getActiveCatalogs() {
        return ResponseEntity.ok(serviceCatalogService.getActiveCatalogs());
    }

    /**
     * GET /api/service-catalogs/{id}
     * Mengembalikan detail katalog layanan berdasarkan ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCatalogById(@PathVariable Long id) {
        return serviceCatalogService.getCatalogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/service-catalogs
     * Membuat atau menyimpan katalog layanan baru (hanya dapat diakses oleh ADMIN atau TECHNICIAN).
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @PostMapping
    public ResponseEntity<?> createCatalog(@RequestBody ServiceCatalog catalog) {
        try {
            ServiceCatalog created = serviceCatalogService.createCatalog(catalog);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/service-catalogs/{id}
     * Menghapus katalog layanan berdasarkan ID (hanya dapat diakses oleh ADMIN atau TECHNICIAN).
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCatalog(@PathVariable Long id) {
        try {
            serviceCatalogService.deleteCatalog(id);
            return ResponseEntity.ok(Map.of("message", "Katalog layanan berhasil dihapus"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
