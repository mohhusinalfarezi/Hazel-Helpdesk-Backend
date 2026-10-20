package com.chatkeluhan.demo.controller;

import com.chatkeluhan.demo.entity.Asset;
import com.chatkeluhan.demo.service.AssetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    /**
     * GET /api/assets/my
     * Mengambil email pengguna yang sedang login dari JWT/SecurityContext,
     * memanggil assetService.getMyAssets(userEmail), dan mengembalikan List<Asset>.
     */
    @GetMapping("/my")
    public ResponseEntity<List<Asset>> getMyAssets() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (authentication != null) ? authentication.getName() : null;

        List<Asset> assets = assetService.getMyAssets(userEmail);
        return ResponseEntity.ok(assets);
    }

    /**
     * GET /api/assets
     * Mengembalikan seluruh daftar aset kantor (dapat diakses oleh role ADMIN atau TECHNICIAN).
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @GetMapping
    public ResponseEntity<List<Asset>> getAllAssets() {
        List<Asset> assets = assetService.getAllAssets();
        return ResponseEntity.ok(assets);
    }

    /**
     * POST /api/assets
     * Menerima payload JSON Asset baru, simpan ke database, dan kembalikan response 200 OK.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @PostMapping
    public ResponseEntity<Asset> createAsset(@RequestBody Asset asset) {
        Asset savedAsset = assetService.createAsset(asset);
        return ResponseEntity.ok(savedAsset);
    }
}
