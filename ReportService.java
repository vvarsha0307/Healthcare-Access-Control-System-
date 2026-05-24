package com.example.demo.service;
import com.example.demo.model.Report;
import com.example.demo.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
@Service
public class ReportService {
    @Autowired
    private ReportRepository reportRepository;
    // =====================================================
    // 1️⃣ GET REPORT BY PATIENT ID
    // =====================================================
    public Report getReportByPatientId(Long patientId) {
        return reportRepository
                .findFirstByPatientIdOrderByReportIdDesc(patientId)
                .orElse(null);
    }
    // =====================================================
    // 2️⃣ UPDATE REPORT (DOCTOR)
    // =====================================================
    public String updateReport(Long patientId,
                               Long doctorId,
                               String diagnosis,
                               String treatment) {
        Report report = getReportByPatientId(patientId);
        // report not found
        if (report == null) {
            return "Report not found ❌";
        }
        // locked check
        if (report.isLocked()) {
            return "Report is locked ❌";
        }
        // update data
        report.setDiagnosis(diagnosis);
        report.setTreatment(treatment);
        report.setDoctorId(doctorId);
        report.setStatus("PENDING");
        report.setVersion(report.getVersion() + 1);
        reportRepository.save(report);
        return "Report updated successfully ✅";
    }
    // =====================================================
    // 3️⃣ DOCTOR DIGITAL APPROVAL
    // =====================================================
    public String approveReport(Long patientId,
                                Long doctorId) {
        Report report = getReportByPatientId(patientId);
        if (report == null) {
            return "Report not found ❌";
        }
        // lock after approval
        report.setStatus("APPROVED");
        report.setDoctorId(doctorId);
        report.setLocked(true);
        reportRepository.save(report);
        return "Doctor Approved & Locked ✅";
    }

    // =====================================================
    // 4️⃣ CREATE NEW REPORT (OPTIONAL)
    // =====================================================
    public Report createReport(Long patientId,
                               String diagnosis,
                               String treatment,
                               Long doctorId) {
        Report report = new Report();
        report.setPatientId(patientId);
        report.setDiagnosis(diagnosis);
        report.setTreatment(treatment);
        report.setDoctorId(doctorId);
        report.setStatus("PENDING");
        report.setCreatedAt(LocalDateTime.now());
        report.setLocked(false);
        report.setVersion(0);
        return reportRepository.save(report);
    }
    public String nurseUpdateAttempt(Long patientId) {

        Report report = getReportByPatientId(patientId);

        if (report == null) {
            return "Report not found ❌";
        }

        if (report.isLocked()) {
            return "ACCESS DENIED ❌ Doctor already approved (LOCKED)";
        }

        return "Nurse can update ✔";
    }

}
