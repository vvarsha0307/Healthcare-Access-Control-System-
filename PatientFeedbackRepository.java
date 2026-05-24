package com.example.demo.repository;

import com.example.demo.model.PatientFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PatientFeedbackRepository extends JpaRepository<PatientFeedback, Long> {

    List<PatientFeedback> findByDeletedFalse();

    List<PatientFeedback> findByStatusAndDeletedFalse(String status);

    List<PatientFeedback> findByDepartmentAndDeletedFalse(String department);

    List<PatientFeedback> findByDoctorNameAndDeletedFalse(String doctorName);

    List<PatientFeedback> findByEmailAndDeletedFalse(String email);

    List<PatientFeedback> findByVisitDateBetweenAndDeletedFalse(LocalDate startDate, LocalDate endDate);

    List<PatientFeedback> findByOverallRatingGreaterThanAndDeletedFalse(Integer rating);

    long countByStatusAndDeletedFalse(String status);

    long countByRecommendAndDeletedFalse(String recommend);

    List<PatientFeedback> findByStatusAndDeletedFalseOrderBySubmittedAtAsc(String status);

    List<PatientFeedback> findByDepartmentAndDeletedFalseOrderBySubmittedAtDesc(String department);
}