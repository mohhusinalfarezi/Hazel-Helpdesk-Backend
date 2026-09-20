package com.chatkeluhan.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    // --- Konstruktor Kosong (Wajib untuk JPA) ---
    public Customer() {
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
}