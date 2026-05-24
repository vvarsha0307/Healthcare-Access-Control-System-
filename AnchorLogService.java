package com.example.demo.service;

import com.example.demo.entity.AnchorLog;
import com.example.demo.repository.AnchorLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AnchorLogService {

    @Autowired
    private AnchorLogRepository repository;

    // =========================
    // SAVE EVENT
    // =========================
    public void saveEvent(Long userId,
                          Long patientId,
                          String event) {

        AnchorLog log = new AnchorLog();
        log.setUserId(userId);
        log.setPatientId(patientId);
        log.setAnchorEvent(event);
        log.setTimestamp(LocalDateTime.now());

        repository.save(log);
    }

    // =========================
    // 🔥 GET LAST EVENT
    // =========================
    public String getLastEvent(Long userId, Long patientId) {

        return repository
                .findTopByUserIdAndPatientIdOrderByTimestampDesc(userId, patientId)
                .map(AnchorLog::getAnchorEvent)
                .orElse(null);
    }

    // =========================
    // 🔥 STRICT FLOW CHECK
    // =========================
    public boolean isValidFlow(Long userId, Long patientId, String nextAction) {

        String lastEvent = getLastEvent(userId, patientId);

        System.out.println("LAST EVENT: " + lastEvent);
        System.out.println("NEXT ACTION: " + nextAction);

        // 🔥 STEP 1 → must view vitals first
        if ("DIAGNOSED".equals(nextAction)) {
            return "VIEWED_VITALS".equals(lastEvent);
        }

        // 🔥 STEP 2 → must diagnose before signing
        if ("SIGNED".equals(nextAction)) {
            return "DIAGNOSED".equals(lastEvent);
        }

        return true;
    }
}