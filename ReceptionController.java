package com.example.demo.controller;

import com.example.demo.model.Appointment;
import com.example.demo.model.Patient;
import com.example.demo.model.PatientFeedback;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.DiagnosisRepository;
import com.example.demo.repository.PatientRepository;
import com.example.demo.service.AppointmentService;
import com.example.demo.service.PatientFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reception")
@CrossOrigin(origins = "*")
public class ReceptionController {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DiagnosisRepository diagnosisRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PatientFeedbackService patientFeedbackService;

    // ========== PATIENT ENDPOINTS ==========

    @PostMapping("/create-patient")
    public ResponseEntity<?> createPatient(@RequestBody Patient p) {
        p.setStatus("ADMITTED");
        p.setDeleted(false);
        patientRepository.save(p);
        return ResponseEntity.ok(Map.of("message", "✅ Patient admitted successfully"));
    }

    @GetMapping("/patients")
    public ResponseEntity<?> getAllPatients() {
        List<Patient> patients = patientRepository.findByDeletedFalse();
        Map<String, Object> response = new HashMap<>();
        response.put("patients", patients);
        response.put("totalPatients", patients.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/discharged")
    public ResponseEntity<?> getDischarged() {
        List<Patient> discharged = patientRepository.findByStatusAndDeletedFalse("DISCHARGED");
        return ResponseEntity.ok(discharged);
    }

    @PostMapping("/checkout/{patientId}")
    @Transactional
    public ResponseEntity<?> checkoutPatient(@PathVariable Long patientId) {
        Patient p = patientRepository.findById(patientId).orElse(null);
        if (p == null) return ResponseEntity.status(404).body("Patient not found");
        p.setStatus("DISCHARGED");
        p.setDischargeTime(LocalDateTime.now());
        patientRepository.save(p);
        return ResponseEntity.ok(Map.of("message", "✅ Patient checked out successfully"));
    }

    // ========== APPROVED FOR DISCHARGE ==========

    @GetMapping("/approved-for-discharge")
    public ResponseEntity<?> getApprovedForDischarge() {
        List<Patient> admitted = patientRepository.findByStatusAndDeletedFalse("ADMITTED");

        List<Map<String, Object>> result = admitted.stream()
                .filter(p -> diagnosisRepository
                        .findByPatientId(p.getPatientId())
                        .map(d -> d.isDoctorSigned())
                        .orElse(false))
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("patientId",            p.getPatientId());
                    m.put("patientName",          p.getPatientName());
                    m.put("department",           p.getDepartment());
                    m.put("age",                  p.getAge());
                    m.put("gender",               p.getGender());
                    m.put("diseaseType",          p.getDiseaseType());
                    m.put("status",               p.getStatus());
                    m.put("approvedForDischarge", true);
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ========== APPOINTMENT ENDPOINTS ==========

    @PostMapping("/create-appointment")
    @Transactional
    public ResponseEntity<?> createAppointment(@RequestBody Appointment appointment) {
        try {
            if (appointment.getPatientName() == null || appointment.getPatientName().isEmpty())
                return ResponseEntity.badRequest().body("Patient name required");
            if (appointment.getEmail() == null || appointment.getEmail().isEmpty())
                return ResponseEntity.badRequest().body("Email required");
            if (appointment.getDepartment() == null || appointment.getDepartment().isEmpty())
                return ResponseEntity.badRequest().body("Department required");
            if (appointment.getDoctorName() == null || appointment.getDoctorName().isEmpty())
                return ResponseEntity.badRequest().body("Doctor required");
            if (appointment.getAppointmentDate() == null)
                return ResponseEntity.badRequest().body("Date required");
            if (appointment.getAppointmentTime() == null)
                return ResponseEntity.badRequest().body("Time required");

            Appointment saved = appointmentService.createAppointment(appointment);
            Map<String, Object> response = new HashMap<>();
            response.put("success",            true);
            response.put("message",            "Appointment scheduled successfully");
            response.put("confirmationNumber", saved.getConfirmationNumber());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> getAppointment(@PathVariable Long appointmentId) {
        return appointmentService.getAppointmentById(appointmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).build());
    }

    @PutMapping("/update-appointment/{appointmentId}")
    @Transactional
    public ResponseEntity<?> updateAppointment(@PathVariable Long appointmentId,
                                               @RequestBody Appointment updates) {
        try {
            Appointment updated = appointmentService.updateAppointment(appointmentId, updates);
            return ResponseEntity.ok(Map.of("message", "Appointment updated successfully",
                    "appointment", updated));
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/cancel-appointment/{appointmentId}")
    @Transactional
    public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId) {
        try {
            appointmentService.cancelAppointment(appointmentId);
            return ResponseEntity.ok(Map.of("message", "Appointment cancelled successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        }
    }

    // ========== FEEDBACK ENDPOINTS ==========

    @PostMapping("/submit-feedback")
    @Transactional
    public ResponseEntity<?> submitFeedback(@RequestBody PatientFeedback feedback) {
        try {
            if (feedback.getPatientName() == null || feedback.getPatientName().isEmpty())
                return ResponseEntity.badRequest().body("Patient name required");
            if (feedback.getEmail() == null || feedback.getEmail().isEmpty())
                return ResponseEntity.badRequest().body("Email required");
            if (feedback.getOverallRating() == null)
                return ResponseEntity.badRequest().body("Overall rating required");
            if (feedback.getComments() == null || feedback.getComments().isEmpty())
                return ResponseEntity.badRequest().body("Comments required");

            PatientFeedback saved = patientFeedbackService.submitFeedback(feedback);
            Map<String, Object> response = new HashMap<>();
            response.put("success",    true);
            response.put("message",    "Thank you! Your feedback has been recorded");
            response.put("feedbackId", saved.getFeedbackId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/feedback-list")
    public ResponseEntity<?> getFeedbackList() {
        return ResponseEntity.ok(patientFeedbackService.getAllFeedback());
    }

    @GetMapping("/feedback/{feedbackId}")
    public ResponseEntity<?> getFeedback(@PathVariable Long feedbackId) {
        return patientFeedbackService.getFeedbackById(feedbackId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).build());
    }

    @GetMapping("/feedback-stats")
    public ResponseEntity<?> getFeedbackStats() {
        return ResponseEntity.ok(patientFeedbackService.getFeedbackStatistics());
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<?> getDashboardStats() {
        List<Patient> allPatients = patientRepository.findByDeletedFalse();
        List<Patient> discharged  = patientRepository.findByStatusAndDeletedFalse("DISCHARGED");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPatients",     allPatients.size());
        stats.put("dischargedToday",   discharged.size());
        stats.put("appointmentsToday", appointmentService.countAppointmentsForToday());
        stats.put("pendingFeedback",   patientFeedbackService.countFeedbackByStatus("Pending"));

        return ResponseEntity.ok(stats);
    }
}