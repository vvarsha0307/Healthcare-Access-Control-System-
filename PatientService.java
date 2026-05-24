package com.example.demo.service;

import com.example.demo.model.Patient;
import com.example.demo.model.Vitals;
import com.example.demo.repository.PatientRepository;
import com.example.demo.repository.VitalsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final VitalsRepository vitalsRepository;

    public PatientService(PatientRepository patientRepository,
                          VitalsRepository vitalsRepository) {
        this.patientRepository = patientRepository;
        this.vitalsRepository  = vitalsRepository;
    }

    // ✅ Fixed: DB-level filter instead of Java stream
    public List<Patient> getAllActivePatients() {
        return patientRepository.findByDeletedFalse();
    }

    public List<Patient> getPatientsByDepartment(String department) {
        return patientRepository.findByDepartmentAndDeletedFalse(department);
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id).orElse(null);
    }

    public Patient addPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public Patient updatePatient(Long id, Patient updated) {
        Patient existing = patientRepository.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setPatientName(updated.getPatientName());
        existing.setDepartment(updated.getDepartment());
        existing.setAge(updated.getAge());
        existing.setGender(updated.getGender());
        existing.setDiseaseType(updated.getDiseaseType());
        return patientRepository.save(existing);
    }

    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id).orElse(null);
        if (patient != null) {
            patient.setDeleted(true);
            patientRepository.save(patient);
        }
    }

    // ✅ N+1 Fix: ONE query for all patients' latest vitals
    // Use this in dashboard controllers instead of looping vitalsRepo per patient
    public Map<Long, Vitals> getLatestVitalsForPatients(List<Patient> patients) {
        List<Long> patientIds = patients.stream()
                .map(Patient::getPatientId)
                .toList();

        List<Vitals> allVitals = vitalsRepository.findLatestVitalsForPatients(patientIds);

        return allVitals.stream()
                .collect(Collectors.toMap(
                        Vitals::getPatientId,
                        v -> v,
                        (existing, replacement) -> existing
                ));
    }
}