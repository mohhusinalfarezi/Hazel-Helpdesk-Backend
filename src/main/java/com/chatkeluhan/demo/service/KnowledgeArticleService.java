package com.chatkeluhan.demo.service;

import com.chatkeluhan.demo.entity.KnowledgeArticle;
import com.chatkeluhan.demo.repository.KnowledgeArticleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class KnowledgeArticleService {

    private final KnowledgeArticleRepository knowledgeArticleRepository;

    public KnowledgeArticleService(KnowledgeArticleRepository knowledgeArticleRepository) {
        this.knowledgeArticleRepository = knowledgeArticleRepository;
    }

    public List<KnowledgeArticle> getAllArticles() {
        return knowledgeArticleRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<KnowledgeArticle> getArticlesByCategory(String category) {
        if (category == null || category.trim().isEmpty() || "Semua".equalsIgnoreCase(category.trim())) {
            return getAllArticles();
        }
        return knowledgeArticleRepository.findByCategoryIgnoreCaseOrderByCreatedAtDesc(category.trim());
    }

    public Optional<KnowledgeArticle> getArticleById(Long id) {
        return knowledgeArticleRepository.findById(id);
    }

    public KnowledgeArticle createArticle(KnowledgeArticle article) {
        return knowledgeArticleRepository.save(article);
    }

    public void deleteArticle(Long id) {
        knowledgeArticleRepository.deleteById(id);
    }

    @PostConstruct
    public void initDefaultArticles() {
        try {
            if (knowledgeArticleRepository.count() == 0) {
                KnowledgeArticle a1 = new KnowledgeArticle(
                    "Panduan Reset Router & Masalah Koneksi Wi-Fi",
                    "Jaringan",
                    "Jika koneksi internet kantor atau area kerja terputus, langkah awal yang dapat dilakukan:\n" +
                    "1. Pastikan lampu indikator WAN/Internet pada router menyala hijau stabil.\n" +
                    "2. Cabut kabel adaptor daya router selama 30 detik lalu pasang kembali.\n" +
                    "3. Pada laptop/komputer, matikan adapter Wi-Fi lalu nyalakan kembali atau gunakan perintah 'ipconfig /renew' di command prompt.\n" +
                    "4. Jika masalah berlanjut, hubungi tim helpdesk dengan menyertakan lokasi access point.",
                    "Tim Network Support"
                );
                a1.setCreatedAt(LocalDateTime.now().minusDays(3));

                KnowledgeArticle a2 = new KnowledgeArticle(
                    "Cara Mengatasi Masalah Printer Offline di Windows",
                    "Hardware",
                    "Kendala printer berstatus 'Offline' pada sistem operasi Windows sering terjadi karena antrian cetak tersangkut:\n" +
                    "1. Buka Control Panel -> Devices and Printers.\n" +
                    "2. Klik kanan printer yang bermasalah -> See what's printing.\n" +
                    "3. Klik menu 'Printer' dan pastikan opsi 'Use Printer Offline' tidak dicentang.\n" +
                    "4. Restart service Print Spooler melalui menu services.msc jika dokumen tidak mau terhapus.\n" +
                    "5. Periksa kabel USB atau koneksi IP printer pada jaringan lokal.",
                    "Dukungan Perangkat Keras"
                );
                a2.setCreatedAt(LocalDateTime.now().minusDays(2));

                KnowledgeArticle a3 = new KnowledgeArticle(
                    "Langkah Pengaturan VPN Kantor pada Laptop",
                    "Software",
                    "Untuk mengakses sistem internal kantor dari luar kantor (WFH):\n" +
                    "1. Unduh dan pasang aplikasi VPN Client resmi kantor yang telah disediakan di portal IT.\n" +
                    "2. Masukkan alamat server VPN kantor: vpn.kantor.co.id.\n" +
                    "3. Masukkan kredensial login akun korporat Anda (email kantor dan password akun SSO).\n" +
                    "4. Masukkan kode 2FA (Two-Factor Authentication) dari aplikasi Authenticator di ponsel Anda.\n" +
                    "5. Setelah status terhubung (Connected), Anda dapat membuka aplikasi internal.",
                    "Tim Keamanan IT"
                );
                a3.setCreatedAt(LocalDateTime.now().minusDays(1));

                KnowledgeArticle a4 = new KnowledgeArticle(
                    "Prosedur Permintaan Perangkat Kerja Baru",
                    "Hardware",
                    "Bagi karyawan yang membutuhkan penambahan periferal (monitor tambahan, keyboard, mouse, headset) atau penggantian laptop:\n" +
                    "1. Buat tiket permohonan melalui aplikasi helpdesk ini dengan memilih kategori 'Hardware'.\n" +
                    "2. Lampirkan persetujuan (approval) atasan langsung di dalam deskripsi tiket.\n" +
                    "3. Tim IT Asset akan memvalidasi stok perangkat yang tersedia dalam waktu 1x24 jam kerja.\n" +
                    "4. Pengambilan perangkat dapat dilakukan di ruang IT Ruang 302 setelah status tiket berstatus 'READY'.",
                    "IT Asset Management"
                );
                a4.setCreatedAt(LocalDateTime.now().minusHours(12));

                knowledgeArticleRepository.saveAll(List.of(a1, a2, a3, a4));
            }
        } catch (Exception e) {
            System.err.println("Gagal inisialisasi data default KnowledgeArticle: " + e.getMessage());
        }
    }
}
