package com.chatkeluhan.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TICKET_MESSAGE")
public class TicketMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    @JsonIgnoreProperties({"customer", "asset", "category", "technician"})
    private Ticket ticket;

    @Column(name = "sender", nullable = false)
    private String sender;

    @Column(name = "sender_email", length = 100, nullable = false)
    private String senderEmail;

    @Column(name = "sender_name", length = 100, nullable = false)
    private String senderName;

    @Column(name = "sender_role", length = 20, nullable = false)
    private String senderRole; // "USER" atau "TECHNICIAN"

    @Column(name = "message_text", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.sender == null || this.sender.isBlank()) {
            this.sender = this.senderEmail != null ? this.senderEmail : "USER";
        }
    }

    public TicketMessage() {}

    // --- Getter dan Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
