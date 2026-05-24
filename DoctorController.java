package com.example.demo.controller;

import com.example.demo.DTO.DiagnosisRequest;
import com.example.demo.model.*;
import com.example.demo.repository.DiagnosisRepository;
import com.example.demo.security.JwtUserService;
import com.example.demo.security.session.UserSessionRepository;
import com.example.demo.service.*;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctor")
@CrossOrigin(origins = "*")
public class DoctorController {

    private final PatientService patientService;
    private final VitalsService vitalsService;
    private final JwtUserService jwtUserService;
    private final AnchorLogService anchorLogService;
    private final DiagnosisRepository diagnosisRepo;
    private final ShiftAccessService shiftAccessService;
    private final UserSessionRepository sessionRepo;

    public DoctorController(PatientService patientService,
                            VitalsService vitalsService,
                            JwtUserService jwtUserService,
                            AnchorLogService anchorLogService,
                            DiagnosisRepository diagnosisRepo,
                            ShiftAccessService shiftAccessService,
                            UserSessionRepository sessionRepo) {
        this.patientService     = patientService;
        this.vitalsService      = vitalsService;
        this.jwtUserService     = jwtUserService;
        this.anchorLogService   = anchorLogService;
        this.diagnosisRepo      = diagnosisRepo;
        this.shiftAccessService = shiftAccessService;
        this.sessionRepo        = sessionRepo;
    }

    // =========================
    // TOKEN HELPER
    // =========================
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }

    // =========================
    // FORCE LOGOUT
    // =========================
    private ResponseEntity<?> forceLogout(Long userId, String authHeader, String message) {
        String token = extractToken(authHeader);
        sessionRepo.findFirstByTokenAndActiveTrueOrderByIdDesc(token)
                .ifPresent(session -> {
                    session.setActive(false);
                    sessionRepo.save(session);
                });
        return ResponseEntity.status(403).body("❌ " + message + " — session terminated");
    }

    // =========================
    // SHIFT CHECK
    // =========================
    private boolean isShiftValid(Long userId) {
        return shiftAccessService.isUserWithinShift(userId);
    }

    // =========================
    // NULL-SAFE DEPARTMENT CHECK
    // Only blocks if BOTH sides are non-null AND differ.
    // If doctor has no department in DB → warns but allows (prevents false logouts).
    // =========================
    private boolean isDeptMismatch(String userDept, String patientDept) {
        if (userDept == null || userDept.isBlank()) {
            System.out.println("⚠️ WARNING: Doctor has no department set in DB — skipping dept check.");
            return false;
        }
        if (patientDept == null || patientDept.isBlank()) {
            return false;
        }
        return !userDept.equalsIgnoreCase(patientDept);
    }

    // =========================
    // GET PATIENTS BY DEPARTMENT
    // =========================
    @GetMapping("/patients/department/{dept}")
    public ResponseEntity<?> getPatientsByDept(@PathVariable String dept,
                                               HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        Long userId = jwtUserService.extractUserId(auth);

        if (!isShiftValid(userId)) {
            return forceLogout(userId, auth, "Shift ended");
        }

        List<Patient> patients = patientService.getPatientsByDepartment(dept);
        return ResponseEntity.ok(patients);
    }

    // =========================
    // VIEW VITALS
    // =========================
    @GetMapping("/vitals/{patientId}")
    public ResponseEntity<?> getVitals(@PathVariable Long patientId,
                                       HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        Long userId = jwtUserService.extractUserId(auth);

        if (!isShiftValid(userId)) {
            return forceLogout(userId, auth, "Shift ended");
        }

        Vitals v = vitalsService.getVitalsByPatientId(patientId);

        if (v == null) {
            return ResponseEntity.ok("No vitals recorded");
        }

        anchorLogService.saveEvent(userId, patientId, "VIEWED_VITALS");
        return ResponseEntity.ok(v);
    }

    // =========================
    // DIAGNOSE
    // =========================
    @PutMapping("/diagnose/{patientId}")
    public ResponseEntity<?> diagnose(@PathVariable Long patientId,
                                      @RequestBody DiagnosisRequest body,
                                      HttpServletRequest request) {
        String auth     = request.getHeader("Authorization");
        Long userId     = jwtUserService.extractUserId(auth);
        String userDept = jwtUserService.extractDepartment(auth);

        if (!isShiftValid(userId)) {
            return forceLogout(userId, auth, "Shift ended");
        }

        Patient patient = patientService.getPatientById(patientId);
        if (patient == null) {
            return ResponseEntity.status(404).body("❌ Patient not found");
        }

        // ✅ FIXED: null-safe — won't logout doctor if dept missing from DB
        if (isDeptMismatch(userDept, patient.getDepartment())) {
            return forceLogout(userId, auth, "Cross-department diagnosis not allowed");
        }

        if (!anchorLogService.isValidFlow(userId, patientId, "DIAGNOSED")) {
            return forceLogout(userId, auth, "View vitals first");
        }

        Diagnosis d = diagnosisRepo.findByPatientId(patientId)
                .orElse(new Diagnosis());

        d.setPatientId(patientId);
        d.setDoctorId(userId);
        d.setDiagnosis(body.getDiagnosis());
        d.setTreatment(body.getTreatment());
        d.setDoctorSigned(false);
        diagnosisRepo.save(d);

        anchorLogService.saveEvent(userId, patientId, "DIAGNOSED");
        return ResponseEntity.ok("Diagnosis saved ✅");
    }

    // =========================
    // SIGN REPORT
    // =========================
    @PostMapping("/sign/{patientId}")
    public ResponseEntity<?> signReport(@PathVariable Long patientId,
                                        HttpServletRequest request) {
        String auth     = request.getHeader("Authorization");
        Long userId     = jwtUserService.extractUserId(auth);
        String userDept = jwtUserService.extractDepartment(auth);

        if (!isShiftValid(userId)) {
            return forceLogout(userId, auth, "Shift ended");
        }

        Patient patient = patientService.getPatientById(patientId);
        if (patient == null) {
            return ResponseEntity.status(404).body("❌ Patient not found");
        }

        // ✅ FIXED: null-safe — won't logout doctor if dept missing from DB
        if (isDeptMismatch(userDept, patient.getDepartment())) {
            return forceLogout(userId, auth, "Cross-department signing not allowed");
        }

        if (!anchorLogService.isValidFlow(userId, patientId, "SIGNED")) {
            return forceLogout(userId, auth, "Diagnose first");
        }

        Diagnosis d = diagnosisRepo.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException("❌ No diagnosis found"));

        d.setDoctorSigned(true);
        diagnosisRepo.save(d);

        anchorLogService.saveEvent(userId, patientId, "SIGNED");
        return ResponseEntity.ok("Report signed ✅");
    }
}