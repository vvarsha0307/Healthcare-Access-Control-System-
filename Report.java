package com.example.demo.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "reports")
public class Report {
    // ===============================
    // PRIMARY KEY
    // ===============================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;
    // ===============================
    // BASIC FIELDS
    // ===============================
    @Column(name = "patient_id")
    private Long patientId;
    @Column(name = "diagnosis")
    private String diagnosis;
    @Column(name = "treatment")
    private String treatment;
    @Column(name = "status")
    private String status;
    @Column(name = "doctor_id")
    private Long doctorId;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    // ===============================
    // LOCKING SYSTEM (Future use)
    // ===============================
    @Column(name = "is_locked")
    private boolean isLocked = false;
    @Column(name = "version")
    private Integer version = 0;
    // ===============================
    // GETTERS & SETTERS
    // ===============================
    public Long getReportId() {
        return reportId;
    }
    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }
    public Long getPatientId() {
        return patientId;
    }
    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }
    public String getDiagnosis() {
        return diagnosis;
    }
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
    public String getTreatment() {
        return treatment;
    }
    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Long getDoctorId() {
        return doctorId;
    }
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public boolean isLocked() {
        return isLocked;
    }
    public void setLocked(boolean locked) {
        isLocked = locked;
    }
    public Integer getVersion() {
        return version;
    }
    public void setVersion(Integer version) {
        this.version = version;
    }
}
