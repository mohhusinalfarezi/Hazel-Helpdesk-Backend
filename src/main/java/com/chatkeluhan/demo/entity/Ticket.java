package com.chatkeluhan.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TICKET")
public class Ticket {

    @Id
    @Column(name = "ticket_id", length = 30)
    private String ticketId;

    // Relasi Foreign Key ke entitas Customer
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Relasi Foreign Key ke entitas Asset
    @ManyToOne
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    // Relasi Foreign Key ke entitas ProblemCategory
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ProblemCategory category;

    // Relasi Foreign Key ke entitas Technician (Boleh kosong di awal)
    @ManyToOne
    @JoinColumn(name = "technician_id")
    private Technician technician;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "current_status", length = 20, nullable = false)
    private String currentStatus;

    @Column(name = "report_date", nullable = false)
    private LocalDateTime reportDate;

    public Ticket() {}

    // --- Getter dan Setter ---
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    public ProblemCategory getCategory() { return category; }
    public void setCategory(ProblemCategory category) { this.category = category; }
    public Technician getTechnician() { return technician; }
    public void setTechnician(Technician technician) { this.technician = technician; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
    public LocalDateTime getReportDate() { return reportDate; }
    public void setReportDate(LocalDateTime reportDate) { this.reportDate = reportDate; }
}