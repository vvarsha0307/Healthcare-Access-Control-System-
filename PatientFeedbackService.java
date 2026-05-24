package com.example.demo.service;

import com.example.demo.model.PatientFeedback;
import com.example.demo.repository.PatientFeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PatientFeedbackService {

    @Autowired
    private PatientFeedbackRepository patientFeedbackRepository;

    @Transactional
    public PatientFeedback submitFeedback(PatientFeedback feedback) {
        feedback.setStatus("Pending");
        feedback.setSubmittedAt(LocalDateTime.now());
        feedback.setDeleted(false);
        return patientFeedbackRepository.save(feedback);
    }

    public List<PatientFeedback> getAllFeedback() {
        return patientFeedbackRepository.findByDeletedFalse();
    }

    public Optional<PatientFeedback> getFeedbackById(Long id) {
        Optional<PatientFeedback> feedback = patientFeedbackRepository.findById(id);
        if (feedback.isPresent() && !feedback.get().getDeleted()) {
            return feedback;
        }
        return Optional.empty();
    }

    public List<PatientFeedback> getPendingFeedback() {
        return patientFeedbackRepository.findByStatusAndDeletedFalseOrderBySubmittedAtAsc("Pending");
    }

    public List<PatientFeedback> getFeedbackByDepartment(String department) {
        return patientFeedbackRepository.findByDepartmentAndDeletedFalseOrderBySubmittedAtDesc(department);
    }

    public List<PatientFeedback> getFeedbackByDoctor(String doctorName) {
        return patientFeedbackRepository.findByDoctorNameAndDeletedFalse(doctorName);
    }

    public List<PatientFeedback> getFeedbackByPatientEmail(String email) {
        return patientFeedbackRepository.findByEmailAndDeletedFalse(email);
    }

    public List<PatientFeedback> getFeedbackByStatus(String status) {
        return patientFeedbackRepository.findByStatusAndDeletedFalse(status);
    }

    public List<PatientFeedback> getFeedbackInDateRange(LocalDate startDate, LocalDate endDate) {
        return patientFeedbackRepository.findByVisitDateBetweenAndDeletedFalse(startDate, endDate);
    }

    public List<PatientFeedback> getPositiveFeedback(Integer ratingThreshold) {
        return patientFeedbackRepository.findByOverallRatingGreaterThanAndDeletedFalse(ratingThreshold);
    }

    @Transactional
    public PatientFeedback reviewFeedback(Long id, String reviewedBy) {
        Optional<PatientFeedback> opt = getFeedbackById(id);
        if (!opt.isPresent()) {
            throw new RuntimeException("Feedback not found");
        }
        PatientFeedback feedback = opt.get();
        feedback.setStatus("Reviewed");
        feedback.setReviewedAt(LocalDateTime.now());
        feedback.setReviewedBy(reviewedBy);
        return patientFeedbackRepository.save(feedback);
    }

    @Transactional
    public void archiveFeedback(Long id) {
        Optional<PatientFeedback> opt = getFeedbackById(id);
        if (!opt.isPresent()) {
            throw new RuntimeException("Feedback not found");
        }
        PatientFeedback feedback = opt.get();
        feedback.setStatus("Archived");
        patientFeedbackRepository.save(feedback);
    }

    @Transactional
    public void deleteFeedback(Long id) {
        Optional<PatientFeedback> opt = patientFeedbackRepository.findById(id);
        if (!opt.isPresent()) {
            throw new RuntimeException("Feedback not found");
        }
        PatientFeedback feedback = opt.get();
        feedback.setDeleted(true);
        patientFeedbackRepository.save(feedback);
    }

    public Double getAverageRating() {
        List<PatientFeedback> allFeedback = getAllFeedback();
        if (allFeedback.isEmpty()) {
            return 0.0;
        }
        double sum = allFeedback.stream()
                .mapToDouble(PatientFeedback::getAverageRating)
                .sum();
        return sum / allFeedback.size();
    }

    public Double getAverageRatingByDepartment(String department) {
        List<PatientFeedback> deptFeedback = getFeedbackByDepartment(department);
        if (deptFeedback.isEmpty()) {
            return 0.0;
        }
        double sum = deptFeedback.stream()
                .mapToDouble(PatientFeedback::getAverageRating)
                .sum();
        return sum / deptFeedback.size();
    }

    public long countFeedbackByStatus(String status) {
        return patientFeedbackRepository.countByStatusAndDeletedFalse(status);
    }

    public long countPositiveRecommendations() {
        return patientFeedbackRepository.countByRecommendAndDeletedFalse("yes");
    }

    public long countNegativeRecommendations() {
        return patientFeedbackRepository.countByRecommendAndDeletedFalse("no");
    }

    public Map<String, Object> getFeedbackStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<PatientFeedback> allFeedback = getAllFeedback();

        stats.put("totalFeedback", allFeedback.size());
        stats.put("averageRating", getAverageRating());
        stats.put("pendingCount", countFeedbackByStatus("Pending"));
        stats.put("positiveRecommendations", countPositiveRecommendations());
        stats.put("negativeRecommendations", countNegativeRecommendations());

        long positive = countPositiveRecommendations();
        long total = allFeedback.size();
        double sentimentScore = total > 0 ? (double) positive / total * 100 : 0;
        stats.put("sentimentScore", sentimentScore);

        return stats;
    }
}