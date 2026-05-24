package com.example.demo.repository;

import com.example.demo.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // All patients in a department (not deleted)
    List<Patient> findByDepartmentAndDeletedFalse(String department);

    // All active patients (not deleted)
    List<Patient> findByDeletedFalse();

    // All patients in a department regardless of delete flag
    List<Patient> findByDepartment(String department);

    // Find patients by status (e.g., "ADMITTED", "DISCHARGED")
    List<Patient> findByStatus(String status);

    // Find patients by status and not deleted
    List<Patient> findByStatusAndDeletedFalse(String status);


}