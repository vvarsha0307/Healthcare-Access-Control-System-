package com.example.demo.repository;

import com.example.demo.model.Vitals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VitalsRepository extends JpaRepository<Vitals, Long> {

    // ✅ Used by VitalsService and NurseController
    Optional<Vitals> findTopByPatientIdOrderByCreatedAtDesc(Long patientId);

    // ✅ Used for full vitals history
    List<Vitals> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    // ✅ Bulk fetch — fixes N+1 on dashboard
    @Query("""
        SELECT v FROM Vitals v
        WHERE v.patientId IN :patientIds
        AND v.createdAt = (
            SELECT MAX(v2.createdAt)
            FROM Vitals v2
            WHERE v2.patientId = v.patientId
        )
        ORDER BY v.patientId
    """)
    List<Vitals> findLatestVitalsForPatients(@Param("patientIds") List<Long> patientIds);
}