package com.chatkeluhan.demo.service;

import com.chatkeluhan.demo.entity.Asset;
import com.chatkeluhan.demo.repository.AssetRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AssetService {

    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    /**
     * Mengambil daftar aset yang dimiliki oleh pengguna berdasarkan email.
     */
    public List<Asset> getMyAssets(String email) {
        if (email == null || email.isBlank()) {
            return Collections.emptyList();
        }
        return assetRepository.findByAssignedEmailIgnoreCase(email.trim());
    }

    /**
     * Mengambil seluruh aset.
     */
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    /**
     * Mengambil aset berdasarkan ID.
     */
    public Optional<Asset> getAssetById(Long id) {
        return assetRepository.findById(id);
    }

    /**
     * Menyimpan atau memperbarui data aset.
     */
    public Asset saveAsset(Asset asset) {
        return createAsset(asset);
    }

    /**
     * Membuat aset baru dengan memastikan name dan assetName konsisten.
     */
    public Asset createAsset(Asset asset) {
        if (asset.getName() != null && (asset.getAssetName() == null || asset.getAssetName().isBlank())) {
            asset.setAssetName(asset.getName());
        } else if (asset.getAssetName() != null && (asset.getName() == null || asset.getName().isBlank())) {
            asset.setName(asset.getAssetName());
        }
        if (asset.getAssetTag() == null || asset.getAssetTag().isBlank()) {
            if (asset.getAssetId() != null && !asset.getAssetId().isBlank()) {
                asset.setAssetTag(asset.getAssetId());
            }
        }
        if (asset.getStatus() == null || asset.getStatus().isBlank()) {
            asset.setStatus("IN_USE");
        }
        if (asset.getCreatedAt() == null) {
            asset.setCreatedAt(LocalDateTime.now());
        }
        return assetRepository.save(asset);
    }

    /**
     * Inisialisasi data contoh (seeding) otomatis jika repository masih kosong.
     */
    @PostConstruct
    public void initSampleAssets() {
        try {
            if (assetRepository.count() == 0) {
                Asset asset1 = new Asset();
                asset1.setAssetTag("AST-2026-081");
                asset1.setName("Laptop Lenovo ThinkPad E14");
                asset1.setType("LAPTOP");
                asset1.setSpecs("Core i7 Gen 13, 16GB RAM, 512GB SSD");
                asset1.setAssignedEmail("ilham@gmail.com");
                asset1.setStatus("IN_USE");
                asset1.setCreatedAt(LocalDateTime.now().minusDays(30));

                Asset asset2 = new Asset();
                asset2.setAssetTag("AST-2026-104");
                asset2.setName("Monitor Dell 24 Inch P2422H");
                asset2.setType("MONITOR");
                asset2.setSpecs("Full HD IPS, Height Adjustable");
                asset2.setAssignedEmail("ilham@gmail.com");
                asset2.setStatus("IN_USE");
                asset2.setCreatedAt(LocalDateTime.now().minusDays(20));

                assetRepository.saveAll(List.of(asset1, asset2));
                System.out.println(">>> Sample assets seeded successfully for ilham@gmail.com");
            }
        } catch (Exception e) {
            System.err.println("Gagal inisialisasi sample data aset: " + e.getMessage());
        }
    }
}
