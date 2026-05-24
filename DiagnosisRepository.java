package com.example.demo.repository;

import com.example.demo.model.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {

    Optional<Diagnosis> findByPatientId(Long patientId);

    // ✅ Count all diagnoses where doctorSigned = false
    long countByDoctorSignedFalse();
}