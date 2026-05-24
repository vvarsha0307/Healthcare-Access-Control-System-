package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.PatientService;
import com.example.demo.service.ShiftAccessService;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/nurse")
@CrossOrigin(origins = "*")
public class NurseController {

    private final DiagnosisRepository diagnosisRepo;
    private final PatientRepository patientRepo;
    private final VitalsRepository vitalsRepo;
    private final UserRepository userRepository;
    private final ShiftAccessService shiftAccessService;
    private final PatientService patientService;

    private final Map<String, String> flowTracker = new ConcurrentHashMap<>();

    public NurseController(DiagnosisRepository diagnosisRepo,
                           PatientRepository patientRepo,
                           VitalsRepository vitalsRepo,
                           UserRepository userRepository,
                           ShiftAccessService shiftAccessService,
                           PatientService patientService) {
        this.diagnosisRepo      = diagnosisRepo;
        this.patientRepo        = patientRepo;
        this.vitalsRepo         = vitalsRepo;
        this.userRepository     = userRepository;
        this.shiftAccessService = shiftAccessService;
        this.patientService     = patientService;
    }

    // ── Helpers ──────────────────────────────────────────────

    private User getCurrentUser(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    private boolean isValidShift(User user) {
        if (user == null) return false;
        boolean result = shiftAccessService.isUserWithinShift(user.getUserId());
        System.out.println("🛡️ isValidShift userId=" + user.getUserId()
                + " username=" + user.getUsername()
                + " subRole=" + user.getSubRole()
                + " → " + result);
        return result;
    }

    private boolean isHeadNurse(User user) {
        return user != null && "HEAD_NURSE".equalsIgnoreCase(user.getSubRole());
    }

    private String flowKey(Long userId, Long patientId) {
        return userId + ":" + patientId;
    }

    private String getFlowStep(Long userId, Long patientId) {
        return flowTracker.getOrDefault(flowKey(userId, patientId), "NONE");
    }

    private void setFlowStep(Long userId, Long patientId, String step) {
        flowTracker.put(flowKey(userId, patientId), step);
    }

    private ResponseEntity<?> violation(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error",   "ACCESS_VIOLATED",
                "message", message,
                "action",  "LOGOUT"
        ));
    }

    private ResponseEntity<?> shiftBlocked() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error",   "SHIFT_BLOCKED",
                "message", "You are outside your shift hours.",
                "action",  "SHIFT_END"
        ));
    }

    // =========================
    // GET PATIENTS
    // =========================
    @GetMapping("/patients")
    public ResponseEntity<?> getPatients(Authentication auth) {

        User user = getCurrentUser(auth);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "action", "LOGOUT"));

        if (!isValidShift(user)) return shiftBlocked();

        List<Patient> allPatients;
        String department = user.getDepartment();

        if (isHeadNurse(user)) {
            allPatients = patientRepo.findByDeletedFalse();
        } else if (department != null && !department.isEmpty()) {
            allPatients = patientRepo.findByDepartmentAndDeletedFalse(department);
        } else {
            allPatients = patientRepo.findByDeletedFalse();
        }

        List<Patient> activePatients = allPatients.stream()
                .filter(p -> !"DISCHARGED".equalsIgnoreCase(p.getStatus()))
                .toList();

        long alerts = 0;
        try { alerts = diagnosisRepo.countByDoctorSignedFalse(); } catch (Exception ignored) {}

        long abnormalVitals = 0;
        try {
            Map<Long, Vitals> latestVitalsMap =
                    patientService.getLatestVitalsForPatients(activePatients);
            abnormalVitals = latestVitalsMap.values().stream()
                    .filter(Vitals::isAbnormal)
                    .count();
        } catch (Exception ignored) {}

        Map<String, Object> response = new HashMap<>();
        response.put("totalPatients",  activePatients.size());
        response.put("alerts",         alerts);
        response.put("abnormalVitals", abnormalVitals);
        response.put("department",     isHeadNurse(user) ? "All" : (department != null ? department : "All"));
        response.put("shiftName",      shiftAccessService.getActiveShiftName(user.getUserId()));
        response.put("patients",       activePatients);

        return ResponseEntity.ok(response);
    }

    // =========================
    // GET SINGLE PATIENT DETAIL
    // =========================
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientDetail(@PathVariable Long patientId,
                                              Authentication auth) {

        User user = getCurrentUser(auth);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "action", "LOGOUT"));

        if (!isValidShift(user)) return shiftBlocked();

        Patient p = patientRepo.findById(patientId).orElse(null);
        if (p == null) return ResponseEntity.badRequest().body("❌ Patient not found");

        if (!isHeadNurse(user)) {
            String dept = user.getDepartment();
            if (dept != null && !dept.equalsIgnoreCase(p.getDepartment()))
                return violation("You can only view patients in your department.");
        }

        Vitals latestVitals = vitalsRepo
                .findTopByPatientIdOrderByCreatedAtDesc(patientId)
                .orElse(null);

        Diagnosis diagnosis = isHeadNurse(user)
                ? diagnosisRepo.findByPatientId(patientId).orElse(null)
                : null;

        Map<String, Object> response = new HashMap<>();
        response.put("patient",   p);
        response.put("vitals",    latestVitals);
        response.put("diagnosis", diagnosis);

        return ResponseEntity.ok(response);
    }

    // =========================
    // ENTER VITALS
    // =========================
    @PostMapping("/vitals/{patientId}")
    public ResponseEntity<?> enterVitals(@PathVariable Long patientId,
                                         @RequestBody Vitals vitalsInput,
                                         Authentication auth) {

        User user = getCurrentUser(auth);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "action", "LOGOUT"));

        if (!isValidShift(user)) return shiftBlocked();

        Patient p = patientRepo.findById(patientId).orElse(null);
        if (p == null) return ResponseEntity.badRequest().body("❌ Patient not found");

        if (!isHeadNurse(user)) {
            String dept = user.getDepartment();
            if (dept != null && !dept.equalsIgnoreCase(p.getDepartment()))
                return violation("You can only enter vitals for patients in your department.");
        } else {
            String step = getFlowStep(user.getUserId(), patientId);
            if (!"VIEWED_NOTES".equals(step))
                return violation(
                        "Wrong order! You must view Doctor Notes before entering vitals. " +
                                "Required flow: View Notes → Enter Vitals → Discharge."
                );
        }

        if (vitalsInput.getBp() == null || vitalsInput.getBp().isEmpty())
            return ResponseEntity.badRequest().body("❌ Blood pressure required");
        if (vitalsInput.getTemperature() <= 0)
            return ResponseEntity.badRequest().body("❌ Temperature required");
        if (vitalsInput.getPulse() <= 0)
            return ResponseEntity.badRequest().body("❌ Pulse required");

        Vitals vitals = new Vitals();
        vitals.setPatientId(patientId);
        vitals.setBp(vitalsInput.getBp());
        vitals.setTemperature(vitalsInput.getTemperature());
        vitals.setPulse(vitalsInput.getPulse());
        vitals.setCreatedAt(LocalDateTime.now());
        vitalsRepo.save(vitals);

        if (isHeadNurse(user))
            setFlowStep(user.getUserId(), patientId, "ENTERED_VITALS");

        Map<String, Object> response = new HashMap<>();
        response.put("message",  "Vitals saved ✅");
        response.put("abnormal", vitals.isAbnormal());
        if (vitals.isAbnormal())
            response.put("warning", "⚠️ Abnormal vitals detected! Doctor attention required.");

        return ResponseEntity.ok(response);
    }

    // =========================
    // VIEW DOCTOR NOTES (HEAD NURSE only)
    // =========================
    @GetMapping("/doctor-notes/{patientId}")
    public ResponseEntity<?> viewDoctorNotes(@PathVariable Long patientId,
                                             Authentication auth) {

        User user = getCurrentUser(auth);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "action", "LOGOUT"));

        if (!isValidShift(user)) return shiftBlocked();

        if (!isHeadNurse(user))
            return violation("You are not authorized to view doctor notes.");

        Diagnosis d = diagnosisRepo.findByPatientId(patientId).orElse(null);
        if (d == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("❌ No diagnosis found for this patient");

        setFlowStep(user.getUserId(), patientId, "VIEWED_NOTES");
        return ResponseEntity.ok(d);
    }

    // =========================
    // DISCHARGE (HEAD NURSE only)
    // =========================
    @PostMapping("/approve-report/{patientId}")
    public ResponseEntity<?> approveDischarge(@PathVariable Long patientId,
                                              Authentication auth) {

        User user = getCurrentUser(auth);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "action", "LOGOUT"));

        if (!isValidShift(user)) return shiftBlocked();

        if (!isHeadNurse(user))
            return violation("Only Head Nurse can discharge patients.");

        String step = getFlowStep(user.getUserId(), patientId);
        if (!"ENTERED_VITALS".equals(step)) {
            String msg = "NONE".equals(step) || step.isEmpty()
                    ? "Wrong order! View Doctor Notes and enter Vitals first."
                    : "VIEWED_NOTES".equals(step)
                    ? "Wrong order! Enter Vitals before discharge."
                    : "Wrong flow step. Required: View Notes → Enter Vitals → Discharge.";
            return violation(msg);
        }

        Vitals v = vitalsRepo.findTopByPatientIdOrderByCreatedAtDesc(patientId).orElse(null);
        if (v == null)
            return ResponseEntity.badRequest().body(Map.of("error", "❌ No vitals recorded yet"));

        Diagnosis d = diagnosisRepo.findByPatientId(patientId).orElse(null);
        if (d == null)
            return ResponseEntity.badRequest().body(Map.of("error", "❌ No diagnosis found"));
        if (!d.isDoctorSigned())
            return ResponseEntity.badRequest().body(Map.of("error", "❌ Doctor has not signed yet"));

        Patient p = patientRepo.findById(patientId).orElse(null);
        if (p == null)
            return ResponseEntity.badRequest().body(Map.of("error", "❌ Patient not found"));

        p.setStatus("DISCHARGED");
        p.setDischargeTime(LocalDateTime.now());
        patientRepo.save(p);

        flowTracker.remove(flowKey(user.getUserId(), patientId));

        return ResponseEntity.ok(Map.of("message", "Patient Discharged 🏥", "patientId", patientId));
    }

    // =========================
    // TRANSFER (HEAD NURSE only)
    // =========================
    @PutMapping("/transfer/{patientId}")
    public ResponseEntity<?> transferPatient(@PathVariable Long patientId,
                                             @RequestBody Map<String, String> body,
                                             Authentication auth) {

        User user = getCurrentUser(auth);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "action", "LOGOUT"));

        if (!isValidShift(user)) return shiftBlocked();

        if (!isHeadNurse(user))
            return violation("Only Head Nurse can transfer patients.");

        String newDept = body.get("department");
        if (newDept == null || newDept.isEmpty())
            return ResponseEntity.badRequest().body("❌ Department required");

        Patient p = patientRepo.findById(patientId).orElse(null);
        if (p == null)
            return ResponseEntity.badRequest().body("❌ Patient not found");

        String oldDept = p.getDepartment();
        p.setDepartment(newDept);
        patientRepo.save(p);

        return ResponseEntity.ok(Map.of(
                "message", "Patient transferred ✅",
                "from",    oldDept != null ? oldDept : "Unknown",
                "to",      newDept
        ));
    }
}