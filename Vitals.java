package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vitals")
public class Vitals {

    @Id
    @Column(name = "vital_id")   // ✅ matches DB
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "bp")
    private String bp;

    @Column(name = "temperature")
    private double temperature;

    @Column(name = "pulse")
    private int pulse;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Vitals() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getBp() { return bp; }
    public void setBp(String bp) { this.bp = bp; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public int getPulse() { return pulse; }
    public void setPulse(int pulse) { this.pulse = pulse; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Check if vitals are abnormal
    public boolean isAbnormal() {
        if (this.temperature > 39 || this.pulse > 120) return true;
        if (this.bp != null && this.bp.toLowerCase().contains("high")) return true;
        return false;
    }
}