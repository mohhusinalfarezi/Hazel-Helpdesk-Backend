package com.chatkeluhan.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TECHNICIAN")
public class Technician {

    @Id
    @Column(name = "technician_id", length = 20)
    private String technicianId;

    @Column(name = "technician_name", length = 100, nullable = false)
    private String technicianName;

    @Column(name = "specialty", length = 100)
    private String specialty;

    public Technician() {}

    public String getTechnicianId() { return technicianId; }
    public void setTechnicianId(String technicianId) { this.technicianId = technicianId; }
    public String getTechnicianName() { return technicianName; }
    public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
}