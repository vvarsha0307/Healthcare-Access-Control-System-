package com.example.demo.repository;
import com.example.demo.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    // =====================================================
    // 1️⃣ Latest report by patientId
    // =====================================================
    Optional<Report> findFirstByPatientIdOrderByReportIdDesc(Long patientId);
    // =====================================================
    // 2️⃣ All reports of patient (optional future use)
    // =====================================================
    List<Report> findByPatientId(Long patientId);
    // =====================================================
    // 3️⃣ Check approved reports (future nurse/admin use)
    // =====================================================
    List<Report> findByStatus(String status);
}
