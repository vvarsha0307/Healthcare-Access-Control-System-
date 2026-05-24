package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class PolicyService {

    // =========================
    // POLICY ENGINE
    // =========================
    public boolean isAllowed(String role,
                             String userDept,
                             String patientDept,
                             String action,
                             String resource) {

        // =========================
        // ONLY FOR DOCTOR RULES
        // =========================
        if ("DOCTOR".equalsIgnoreCase(role)) {

            // ✅ VIEW → ALL DOCTORS CAN VIEW
            if (action.equalsIgnoreCase("VIEW")) {
                return true;
            }

            // ❌ DIAGNOSE / SIGN → ONLY SAME DEPT
            if (action.equalsIgnoreCase("UPDATE_REPORT") ||
                    action.equalsIgnoreCase("SIGN")) {

                if (userDept == null || patientDept == null) return false;

                return userDept.trim().equalsIgnoreCase(patientDept.trim());
            }
        }

        return false;
    }
}