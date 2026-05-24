package com.example.demo.repository;

import com.example.demo.entity.AnchorLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnchorLogRepository extends JpaRepository<AnchorLog, Long> {

    boolean existsByUserIdAndPatientIdAndAnchorEvent(
            Long userId,
            Long patientId,
            String anchorEvent
    );

    // 🔥 GET LATEST EVENT (VERY IMPORTANT)
    Optional<AnchorLog> findTopByUserIdAndPatientIdOrderByTimestampDesc(
            Long userId,
            Long patientId
    );
}