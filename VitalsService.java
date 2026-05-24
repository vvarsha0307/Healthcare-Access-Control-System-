package com.example.demo.service;

import com.example.demo.model.Vitals;
import com.example.demo.repository.VitalsRepository;
import org.springframework.stereotype.Service;

@Service
public class VitalsService {

    private final VitalsRepository vitalsRepository;

    public VitalsService(VitalsRepository vitalsRepository) {
        this.vitalsRepository = vitalsRepository;
    }

    // ✅ Now compiles — method exists in repository
    public Vitals getVitalsByPatientId(Long patientId) {
        return vitalsRepository
                .findTopByPatientIdOrderByCreatedAtDesc(patientId)
                .orElse(null);
    }
}