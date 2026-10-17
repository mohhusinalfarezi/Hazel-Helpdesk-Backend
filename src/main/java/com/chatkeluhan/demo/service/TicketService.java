package com.chatkeluhan.demo.service;

import com.chatkeluhan.demo.entity.*;
import com.chatkeluhan.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.chatkeluhan.demo.dto.ticket.TicketRequest;
import com.chatkeluhan.demo.dto.ticket.TicketResponse;

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
        // Mengambil email dari SecurityContext JWT
        String userEmail = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        
        Customer customer = customerRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Customer tidak ditemukan dengan email: " + userEmail));

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
        Ticket ticket = new Ticket();
        ticket.setTicketId("TKT-" + System.currentTimeMillis());
        ticket.setCustomer(customer);
        ticket.setAsset(asset);
        ticket.setCategory(category);
        ticket.setDescription(masalah);
        ticket.setCurrentStatus("OPEN");
        ticket.setReportDate(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);
        String generatedTicketId = savedTicket.getTicketId();
        
        // Rekam jejak audit secara otomatis
        TicketHistory history = new TicketHistory();
        history.setTicket(savedTicket);
        history.setOldStatus(null);
        history.setNewStatus("OPEN");
        history.setChangeTimestamp(LocalDateTime.now());
        historyRepository.save(history);

        // 3. TAMBAHAN BARU: Panggil fungsi kirim WhatsApp setelah tiket sukses disimpan
        // di MySQL
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
    } // <--- INI KURUNG KURAWAL YANG HILANG SEBELUMNYA

    // --- API METHODS ---

    @Transactional
    public TicketResponse createTicketApi(String email, TicketRequest request) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer tidak ditemukan"));

        Asset asset = null;
        if (request.getAssetId() != null && !request.getAssetId().isBlank()) {
            asset = assetRepository.findById(request.getAssetId()).orElse(null);
        }
        if (asset == null) {
            asset = assetRepository.findAll().stream().findFirst().orElseGet(() -> {
                Asset newAsset = new Asset();
                newAsset.setAssetId("AST-" + System.currentTimeMillis());
                newAsset.setAssetName("General Asset");
                newAsset.setLocationCoordinate("Head Office");
                return assetRepository.save(newAsset);
            });
        }

        ProblemCategory category = null;
        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseGet(() -> categoryRepository.findAll().stream()
                            .filter(c -> c.getCategoryName() != null && c.getCategoryName().equalsIgnoreCase(request.getCategoryId()))
                            .findFirst().orElse(null));
        }
        if (category == null) {
            final String catName = (request.getCategoryId() != null && !request.getCategoryId().isBlank())
                    ? request.getCategoryId() : "General IT";
            category = categoryRepository.findAll().stream().findFirst().orElseGet(() -> {
                ProblemCategory newCat = new ProblemCategory();
                newCat.setCategoryId("CAT-" + System.currentTimeMillis());
                newCat.setCategoryName(catName);
                newCat.setEstimatedResolutionHours(24);
                return categoryRepository.save(newCat);
            });
        }

        Ticket ticket = new Ticket();
        String generatedId = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        ticket.setTicketId(generatedId);

        ticket.setCustomer(customer);
        ticket.setAsset(asset);
        ticket.setCategory(category);
        ticket.setDescription(request.getDescription());
        ticket.setCurrentStatus("OPEN");
        ticket.setReportDate(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        // Simpan history
        TicketHistory history = new TicketHistory();
        history.setTicket(savedTicket);
        history.setOldStatus(null);
        history.setNewStatus("OPEN");
        history.setChangeTimestamp(LocalDateTime.now());
        historyRepository.save(history);

        // Kirim notifikasi in-app: tiket berhasil dibuat
        notificationService.createNotification(
                customer.getEmail(),
                "Tiket Berhasil Dibuat",
                "Tiket keluhan Anda berhasil dikirim dan sedang dalam antrean tim teknis.",
                "TICKET_CREATED",
                savedTicket.getTicketId()
        );

        return mapToResponse(savedTicket);
    }

    public List<TicketResponse> getMyTicketsApi(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer tidak ditemukan"));

        List<Ticket> tickets = ticketRepository.findByCustomer_CustomerId(customer.getCustomerId());

        return tickets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TicketResponse updateTicketStatusApi(String ticketId, String newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Tiket tidak ditemukan"));

        String oldStatus = ticket.getCurrentStatus();
        ticket.setCurrentStatus(newStatus);
        Ticket updatedTicket = ticketRepository.save(ticket);

        // Simpan history
        TicketHistory history = new TicketHistory();
        history.setTicket(updatedTicket);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangeTimestamp(LocalDateTime.now());
        historyRepository.save(history);

        // Kirim notifikasi in-app: status tiket diperbarui
        notificationService.createNotification(
                updatedTicket.getCustomer().getEmail(),
                "Update Status Tiket",
                "Status tiket #" + updatedTicket.getTicketId() + " Anda telah diubah menjadi " + newStatus,
                "TICKET_UPDATE",
                updatedTicket.getTicketId()
        );

        return mapToResponse(updatedTicket);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setTicketId(ticket.getTicketId());
        response.setCustomerName(ticket.getCustomer() != null ? ticket.getCustomer().getFullName() : null);
        response.setAssetName(ticket.getAsset() != null ? ticket.getAsset().getAssetName() : null);
        response.setCategoryName(ticket.getCategory() != null ? ticket.getCategory().getCategoryName() : null);
        response.setDescription(ticket.getDescription());
        response.setCurrentStatus(ticket.getCurrentStatus());
        response.setReportDate(ticket.getReportDate());
        return response;
    }
}