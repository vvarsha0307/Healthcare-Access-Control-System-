package com.example.demo.controller;

import com.example.demo.model.Patient;
import com.example.demo.model.User;
import com.example.demo.repository.PatientRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.PolicyService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/policy")
@CrossOrigin(origins = "*")
public class IntentController {

    private final PolicyService policyService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public IntentController(PolicyService policyService,
                            JwtUtil jwtUtil,
                            UserRepository userRepository,
                            PatientRepository patientRepository) {
        this.policyService = policyService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
    }

    // =========================
    // POLICY CHECK
    // =========================
    @PostMapping("/check")
    public Map<String, String> checkPolicy(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        // =========================
        // 1️⃣ EXTRACT TOKEN
        // =========================
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Map.of("access", "DENIED");
        }

        String token = authHeader.substring(7);

        // =========================
        // 2️⃣ GET USER FROM TOKEN
        // =========================
        String username = jwtUtil.extractUsername(token);

        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return Map.of("access", "DENIED");
        }

        String role = user.getRole().getName();
        String userDept = user.getDepartment();

        // =========================
        // 3️⃣ GET REQUEST DATA
        // =========================
        String intent = request.get("intent");
        String data = request.get("data");

        Long patientId = null;
        if (request.get("patientId") != null) {
            patientId = Long.parseLong(request.get("patientId"));
        }

        // =========================
        // 4️⃣ GET PATIENT DEPARTMENT
        // =========================
        String patientDept = null;

        if (patientId != null) {
            Patient patient = patientRepository.findById(patientId).orElse(null);

            if (patient != null) {
                patientDept = patient.getDepartment();
            }
        }

        // =========================
        // 5️⃣ POLICY CHECK
        // =========================
        boolean allowed = policyService.isAllowed(
                role,
                userDept,
                patientDept,
                intent,
                data
        );

        // =========================
        // 6️⃣ RESPONSE
        // =========================
        Map<String, String> response = new HashMap<>();
        response.put("access", allowed ? "GRANTED" : "DENIED");

        return response;
    }
}