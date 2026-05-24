package com.example.demo.service;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@Service
public class DoctorApprovalService {
    /*
     * 🔵 Temporary in-memory storage
     * Later DB table use pannuvom
     *
     * key = patientId
     * value = signature details
     */
    private final Map<Long, String> digitalSignStore = new HashMap<>();
    // =====================================================
    // 1️⃣ DOCTOR APPROVAL (DIGITAL SIGN)
    // =====================================================
    public void approveReport(Long doctorId,
                              Long patientId,
                              String doctorName) {
        /*
         * Digital signature format:
         * DOCTORNAME + TIME
         */
        String signature =
                "SIGNED_BY_" + doctorName +
                        "_AT_" + LocalDateTime.now();
        digitalSignStore.put(patientId, signature);
        System.out.println("✅ Digital Signature Added: " + signature);
    }
    // =====================================================
    // 2️⃣ VERIFY APPROVAL (NURSE CHECK USE PANVANGA)
    // =====================================================
    public boolean isDoctorSigned(Long patientId) {
        return digitalSignStore.containsKey(patientId);
    }
    // =====================================================
    // 3️⃣ GET SIGNATURE (OPTIONAL DEBUG)
    // =====================================================
    public String getSignature(Long patientId) {
        return digitalSignStore.get(patientId);
    }
}
