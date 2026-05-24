package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_feedback")
public class PatientFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    @Column(nullable = false)
    private String patientName;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(nullable = false)
    private LocalDate visitDate;

    @Column(nullable = false)
    private String department;

    private String doctorName;

    @Column(nullable = false)
    private Integer overallRating;

    @Column(nullable = false)
    private Integer doctorRating;

    @Column(nullable = false)
    private Integer facilityRating;

    @Column(nullable = false)
    private Integer staffRating;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String comments;

    @Column(nullable = false)
    private String recommend;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    private String reviewedBy;

    @Column(nullable = false)
    private String status = "Pending";

    private Boolean deleted = false;

    public PatientFeedback() {
        this.submittedAt = LocalDateTime.now();
    }

    public Long getFeedbackId() {
        return feedbackId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Integer getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(Integer overallRating) {
        this.overallRating = overallRating;
    }

    public Integer getDoctorRating() {
        return doctorRating;
    }

    public void setDoctorRating(Integer doctorRating) {
        this.doctorRating = doctorRating;
    }

    public Integer getFacilityRating() {
        return facilityRating;
    }

    public void setFacilityRating(Integer facilityRating) {
        this.facilityRating = facilityRating;
    }

    public Integer getStaffRating() {
        return staffRating;
    }

    public void setStaffRating(Integer staffRating) {
        this.staffRating = staffRating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getRecommend() {
        return recommend;
    }

    public void setRecommend(String recommend) {
        this.recommend = recommend;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Double getAverageRating() {
        if (overallRating == null || doctorRating == null ||
                facilityRating == null || staffRating == null) {
            return null;
        }
        return (double) (overallRating + doctorRating + facilityRating + staffRating) / 4;
    }

    public Boolean isPositiveRecommendation() {
        return "yes".equalsIgnoreCase(recommend);
    }
}