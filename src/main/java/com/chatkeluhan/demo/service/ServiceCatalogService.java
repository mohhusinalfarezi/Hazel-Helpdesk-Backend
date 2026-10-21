package com.chatkeluhan.demo.service;

import com.chatkeluhan.demo.entity.ServiceCatalog;
import com.chatkeluhan.demo.repository.ServiceCatalogRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceCatalogService {

    private final ServiceCatalogRepository serviceCatalogRepository;

    public ServiceCatalogService(ServiceCatalogRepository serviceCatalogRepository) {
        this.serviceCatalogRepository = serviceCatalogRepository;
    }

    /**
     * Mengembalikan semua katalog layanan yang aktif terurut berdasarkan ID.
     */
    public List<ServiceCatalog> getActiveCatalogs() {
        return serviceCatalogRepository.findByActiveTrueOrderByIdAsc();
    }

    /**
     * Mengembalikan seluruh katalog layanan (aktif maupun non-aktif).
     */
    public List<ServiceCatalog> getAllCatalogs() {
        return serviceCatalogRepository.findAll();
    }

    /**
     * Mengambil detail katalog layanan berdasarkan ID.
     */
    public Optional<ServiceCatalog> getCatalogById(Long id) {
        return serviceCatalogRepository.findById(id);
    }

    /**
     * Menyimpan atau memperbarui data katalog layanan baru.
     */
    public ServiceCatalog createCatalog(ServiceCatalog catalog) {
        return serviceCatalogRepository.save(catalog);
    }

    /**
     * Menghapus katalog layanan berdasarkan ID.
     */
    public void deleteCatalog(Long id) {
        serviceCatalogRepository.deleteById(id);
    }

    /**
     * Mekanisme seeding otomatis saat startup jika repository masih kosong.
     */
    @PostConstruct
    public void initDefaultCatalogs() {
        try {
            if (serviceCatalogRepository.count() == 0) {
                List<ServiceCatalog> defaultCatalogs = List.of(
                    new ServiceCatalog(
                        "Instalasi Perangkat Lunak & Lisensi",
                        "Software",
                        "Permintaan lisensi OS, IDE, Office 365, atau software produktivitas.",
                        "1x24 Jam",
                        "code",
                        true
                    ),
                    new ServiceCatalog(
                        "Pengajuan Hak Akses & VPN",
                        "Network",
                        "Akses jaringan intranet, server database, dan VPN kantor.",
                        "1-2 Hari Kerja",
                        "vpn",
                        true
                    ),
                    new ServiceCatalog(
                        "Peningkatan Hardware & Memori",
                        "Hardware",
                        "Pengajuan upgrade RAM laptop, storage SSD, atau monitor kedua.",
                        "2-3 Hari Kerja",
                        "memory",
                        true
                    ),
                    new ServiceCatalog(
                        "Pembuatan Akun & Email Domain",
                        "General",
                        "Pembuatan akun email perusahaan baru dan integrasi SSO.",
                        "1x24 Jam",
                        "email",
                        true
                    ),
                    new ServiceCatalog(
                        "Pergantian Periferal Kerja",
                        "Hardware",
                        "Permintaan mouse, keyboard eksternal, atau adaptor display baru.",
                        "1x24 Jam",
                        "mouse",
                        true
                    )
                );
                serviceCatalogRepository.saveAll(defaultCatalogs);
            }
        } catch (Exception e) {
            System.err.println("Gagal inisialisasi data default Service Catalog: " + e.getMessage());
        }
    }
}
