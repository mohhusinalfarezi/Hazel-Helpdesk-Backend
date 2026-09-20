package com.chatkeluhan.demo.service;

import com.chatkeluhan.demo.entity.*;
import com.chatkeluhan.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final TicketHistoryRepository historyRepository;
    private final AssetRepository assetRepository;
    private final ProblemCategoryRepository categoryRepository;
    
    // 1. TAMBAHAN BARU: Deklarasi NotificationService
    private final NotificationService notificationService;

    // 2. TAMBAHAN BARU: Injeksi NotificationService ke dalam Constructor
    public TicketService(TicketRepository ticketRepository, 
                         CustomerRepository customerRepository, 
                         TicketHistoryRepository historyRepository,
                         AssetRepository assetRepository,
                         ProblemCategoryRepository categoryRepository,
                         NotificationService notificationService) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.historyRepository = historyRepository;
        this.assetRepository = assetRepository;
        this.categoryRepository = categoryRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public String createTicketFromExtractedData(String noHp, String lokasi, String masalah) {
        Customer customer = customerRepository.findByContactNumber(noHp);
        if (customer == null) {
            customer = new Customer();
            customer.setCustomerId("CUST-" + System.currentTimeMillis());
            customer.setFullName("Pelanggan Baru");
            customer.setContactNumber(noHp);
            customer = customerRepository.save(customer);
        }

        // Dummy Asset based on location
        Asset asset = new Asset();
        asset.setAssetId("AST-" + System.currentTimeMillis());
        asset.setAssetName("Aset di " + lokasi);
        asset.setLocationCoordinate(lokasi);
        asset = assetRepository.save(asset);

        // Dummy Category based on problem
        ProblemCategory category = new ProblemCategory();
        category.setCategoryId("CAT-" + System.currentTimeMillis());
        category.setCategoryName("Umum");
        category.setEstimatedResolutionHours(24);
        category = categoryRepository.save(category);

        // Membuat tiket
        Ticket savedTicket = createTicketFromBot(noHp, asset, category, masalah);
        String generatedTicketId = savedTicket.getTicketId();
        
        // 3. TAMBAHAN BARU: Panggil fungsi kirim WhatsApp setelah tiket sukses disimpan di MySQL
        notificationService.sendWhatsAppMessage(noHp, generatedTicketId, masalah);
        
        return generatedTicketId; 
    }

    @Transactional
    public Ticket createTicketFromBot(String contactNumber, Asset asset, ProblemCategory category, String description) {
        // 1. Validasi identitas pelapor berdasarkan nomor kontak
        Customer customer = customerRepository.findByContactNumber(contactNumber);
        if (customer == null) {
            throw new RuntimeException("Nomor tidak terdaftar. Silakan hubungi admin.");
        }

        // 2. Rangkai objek tiket baru
        Ticket ticket = new Ticket();
        ticket.setTicketId("TKT-" + System.currentTimeMillis()); 
        ticket.setCustomer(customer);
        ticket.setAsset(asset);
        ticket.setCategory(category);
        ticket.setDescription(description);
        ticket.setCurrentStatus("OPEN");
        ticket.setReportDate(LocalDateTime.now());

        // Simpan tiket ke database
        Ticket savedTicket = ticketRepository.save(ticket);

        // 3. Rekam jejak audit secara otomatis
        TicketHistory history = new TicketHistory();
        history.setTicket(savedTicket);
        history.setOldStatus(null);
        history.setNewStatus("OPEN");
        history.setChangeTimestamp(LocalDateTime.now());
        
        historyRepository.save(history);

        return savedTicket;
    }

    public List<Ticket> getTicketsByContactNumber(String contactNumber) {
        Customer customer = customerRepository.findByContactNumber(contactNumber);
        if (customer == null) {
            throw new RuntimeException("Pelanggan dengan nomor " + contactNumber + " tidak ditemukan.");
        }
        return ticketRepository.findByCustomer_CustomerId(customer.getCustomerId());
    }

    // FUNGSI UNTUK FITUR TRACKING
    @Transactional(readOnly = true)
    public String checkTicketStatus(String ticketId) {
        return ticketRepository.findById(ticketId)
                .map(ticket -> "Status Saat Ini: " + ticket.getCurrentStatus() + 
                               " | Kategori: " + ticket.getCategory().getCategoryName())
                .orElse("Tiket tidak ditemukan di dalam sistem. Mohon periksa kembali Nomor Tiket Anda.");
    }
}