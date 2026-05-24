package com.example.demo.service;

import com.example.demo.model.Appointment;
import com.example.demo.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        appointment.setConfirmationNumber("APT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        appointment.setStatus("Scheduled");
        appointment.setDeleted(false);
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findByDeletedFalse();
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        Optional<Appointment> appointment = appointmentRepository.findById(id);
        if (appointment.isPresent() && !appointment.get().getDeleted()) {
            return appointment;
        }
        return Optional.empty();
    }

    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByAppointmentDateAndDeletedFalse(date);
    }

    public List<Appointment> getAppointmentsByDoctor(String doctorName) {
        return appointmentRepository.findByDoctorNameAndDeletedFalse(doctorName);
    }

    public List<Appointment> getAppointmentsByDepartment(String department) {
        return appointmentRepository.findByDepartmentAndDeletedFalse(department);
    }

    public List<Appointment> getAppointmentsByStatus(String status) {
        return appointmentRepository.findByStatusAndDeletedFalse(status);
    }

    public List<Appointment> getUpcomingAppointments() {
        return appointmentRepository.findByStatusAndDeletedFalseOrderByAppointmentDateAsc("Scheduled");
    }

    public List<Appointment> getAppointmentsByPatientEmail(String email) {
        return appointmentRepository.findByEmailAndDeletedFalse(email);
    }

    @Transactional
    public Appointment updateAppointment(Long id, Appointment updates) {
        Optional<Appointment> opt = getAppointmentById(id);
        if (!opt.isPresent()) {
            throw new RuntimeException("Appointment not found");
        }

        Appointment appointment = opt.get();
        if (updates.getPatientName() != null) appointment.setPatientName(updates.getPatientName());
        if (updates.getEmail() != null) appointment.setEmail(updates.getEmail());
        if (updates.getPhone() != null) appointment.setPhone(updates.getPhone());
        if (updates.getDepartment() != null) appointment.setDepartment(updates.getDepartment());
        if (updates.getDoctorName() != null) appointment.setDoctorName(updates.getDoctorName());
        if (updates.getAppointmentDate() != null) appointment.setAppointmentDate(updates.getAppointmentDate());
        if (updates.getAppointmentTime() != null) appointment.setAppointmentTime(updates.getAppointmentTime());
        if (updates.getReasonForVisit() != null) appointment.setReasonForVisit(updates.getReasonForVisit());
        if (updates.getAdditionalNotes() != null) appointment.setAdditionalNotes(updates.getAdditionalNotes());
        if (updates.getStatus() != null) appointment.setStatus(updates.getStatus());

        appointment.setUpdatedAt(LocalDateTime.now());
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public void cancelAppointment(Long id) {
        Optional<Appointment> opt = getAppointmentById(id);
        if (!opt.isPresent()) {
            throw new RuntimeException("Appointment not found");
        }
        Appointment appointment = opt.get();
        appointment.setStatus("Cancelled");
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }

    @Transactional
    public void completeAppointment(Long id) {
        Optional<Appointment> opt = getAppointmentById(id);
        if (!opt.isPresent()) {
            throw new RuntimeException("Appointment not found");
        }
        Appointment appointment = opt.get();
        appointment.setStatus("Completed");
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }

    @Transactional
    public void deleteAppointment(Long id) {
        Optional<Appointment> opt = appointmentRepository.findById(id);
        if (!opt.isPresent()) {
            throw new RuntimeException("Appointment not found");
        }
        Appointment appointment = opt.get();
        appointment.setDeleted(true);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }

    public long countAppointmentsByStatus(String status) {
        return appointmentRepository.countByStatusAndDeletedFalse(status);
    }

    public long countAppointmentsByDepartment(String department) {
        return appointmentRepository.countByDepartmentAndDeletedFalse(department);
    }

    public long countAppointmentsForToday() {
        return appointmentRepository.findByAppointmentDateAndDeletedFalse(LocalDate.now()).size();
    }
}