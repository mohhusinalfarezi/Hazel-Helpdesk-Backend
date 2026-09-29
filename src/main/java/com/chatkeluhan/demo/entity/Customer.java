package com.chatkeluhan.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import java.util.UUID;

@Entity
@Table(name = "CUSTOMER") // Nama tabel harus sama persis dengan yang di database
public class Customer {

    @Id
    @Column(name = "customer_id", length = 20)
    private String customerId;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "contact_number", length = 20, nullable = false)
    private String contactNumber;

    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    // --- Konstruktor Kosong (Wajib untuk JPA) ---
    public Customer() {
    }

    // --- FITUR BARU: Pembuat ID Otomatis Anti-Gagal ---
    @PrePersist
    protected void onCreate() {
        if (this.customerId == null || this.customerId.isEmpty()) {
            // Menghasilkan ID unik otomatis seperti "CUST-A1B2C3D4"
            this.customerId = "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    // --- Getter dan Setter ---
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}