package com.chatkeluhan.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "asset")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "asset_tag", length = 50)
    private String assetTag;

    @Column(name = "name")
    private String name;

    @Column(name = "asset_name")
    private String assetName;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "specs", columnDefinition = "TEXT")
    private String specs;

    @Column(name = "assigned_email")
    private String assignedEmail;

    @Column(name = "status")
    private String status = "IN_USE";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Koordinat / lokasi perangkat untuk kompatibilitas data tiket
    @Column(name = "location_coordinate", length = 255)
    private String locationCoordinate;

    public Asset() {
    }

    public Asset(String assetTag, String name, String type, String specs, String assignedEmail, String status) {
        this.assetTag = assetTag;
        this.name = name;
        this.assetName = name;
        this.type = type;
        this.specs = specs;
        this.assignedEmail = assignedEmail;
        this.status = status != null ? status : "IN_USE";
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        syncNameFields();
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "IN_USE";
        }
    }

    @jakarta.persistence.PreUpdate
    public void preUpdate() {
        syncNameFields();
    }

    private void syncNameFields() {
        if (this.name == null && this.assetName != null) {
            this.name = this.assetName;
        } else if (this.assetName == null && this.name != null) {
            this.assetName = this.name;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAssetTag() {
        return assetTag;
    }

    public void setAssetTag(String assetTag) {
        this.assetTag = assetTag;
    }

    public String getName() {
        return name != null ? name : assetName;
    }

    public void setName(String name) {
        this.name = name;
        if (this.assetName == null) {
            this.assetName = name;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSpecs() {
        return specs;
    }

    public void setSpecs(String specs) {
        this.specs = specs;
    }

    public String getAssignedEmail() {
        return assignedEmail;
    }

    public void setAssignedEmail(String assignedEmail) {
        this.assignedEmail = assignedEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getLocationCoordinate() {
        return locationCoordinate;
    }

    public void setLocationCoordinate(String locationCoordinate) {
        this.locationCoordinate = locationCoordinate;
    }

    // Helper methods untuk kompatibilitas kode tiket yang sudah ada
    public String getAssetId() {
        return assetTag != null ? assetTag : (id != null ? String.valueOf(id) : null);
    }

    public void setAssetId(String assetId) {
        this.assetTag = assetId;
    }

    public String getAssetName() {
        return assetName != null ? assetName : name;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
        if (this.name == null) {
            this.name = assetName;
        }
    }
}