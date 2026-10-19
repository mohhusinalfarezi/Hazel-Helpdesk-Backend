package com.chatkeluhan.demo.service;

import com.chatkeluhan.demo.entity.Announcement;
import com.chatkeluhan.demo.repository.AnnouncementRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    public Announcement createAnnouncement(Announcement announcement) {
        return announcementRepository.save(announcement);
    }

    public void deleteAnnouncement(Long id) {
        announcementRepository.deleteById(id);
    }

    public Optional<Announcement> getAnnouncementById(Long id) {
        return announcementRepository.findById(id);
    }

    @PostConstruct
    public void initDefaultData() {
        try {
            if (announcementRepository.count() == 0) {
                Announcement a1 = new Announcement(
                    "Jadwal Pemeliharaan Server Rutin",
                    "Akan dilakukan pemeliharaan sistem server pada hari Sabtu pukul 23:00 - 03:00 WIB. Layanan tiket keluhan mungkin mengalami latensi sesaat.",
                    "Maintenance",
                    "PENTING",
                    "Admin IT"
                );
                a1.setCreatedAt(LocalDateTime.now().minusHours(5));

                Announcement a2 = new Announcement(
                    "Pembaruan Kebijakan Penanganan Tiket",
                    "Seluruh tiket keluhan kategori Urgent kini memiliki SLA penanganan maksimal 2 jam sejak tiket dilaporkan.",
                    "Kebijakan",
                    "WAJIB",
                    "Helpdesk Lead"
                );
                a2.setCreatedAt(LocalDateTime.now().minusDays(1));

                Announcement a3 = new Announcement(
                    "Rilis Fitur Notifikasi Real-time",
                    "Sistem helpdesk kini dilengkapi dengan notifikasi real-time dan tracking status tiket secara interaktif.",
                    "Sistem",
                    "PEMBARUAN",
                    "Sistem Otomasi"
                );
                a3.setCreatedAt(LocalDateTime.now().minusDays(2));

                announcementRepository.saveAll(List.of(a1, a2, a3));
            }
        } catch (Exception e) {
            System.err.println("Gagal inisialisasi data default Announcement: " + e.getMessage());
        }
    }
}
