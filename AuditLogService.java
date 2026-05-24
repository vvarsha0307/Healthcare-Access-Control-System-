package com.example.demo.service;

import com.example.demo.model.ActivityLog;
import com.example.demo.repository.ActivityLogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    @Autowired
    private ActivityLogRepository repository;

    public void saveLog(Long userId,
                        Long patientId,
                        String username,
                        String role,
                        String action,
                        String result,
                        String ipAddress) {

        ActivityLog log = new ActivityLog();

        log.setUserId(userId);
        log.setPatientId(patientId);
        log.setUsername(username);
        log.setRole(role);
        log.setAction(action);
        log.setResult(result);
        log.setIpAddress(ipAddress);
        log.setTimestamp(LocalDateTime.now());

        repository.save(log);

        System.out.println("✅ Audit Log Saved Successfully");
    }
}