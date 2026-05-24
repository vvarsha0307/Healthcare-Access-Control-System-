package com.example.demo.repository;

import com.example.demo.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDeletedFalse();

    List<Appointment> findByAppointmentDateAndDeletedFalse(LocalDate date);

    List<Appointment> findByDoctorNameAndDeletedFalse(String doctorName);

    List<Appointment> findByDepartmentAndDeletedFalse(String department);

    List<Appointment> findByStatusAndDeletedFalse(String status);

    List<Appointment> findByEmailAndDeletedFalse(String email);

    List<Appointment> findByStatusAndDeletedFalseOrderByAppointmentDateAsc(String status);

    Optional<Appointment> findByConfirmationNumberAndDeletedFalse(String confirmationNumber);

    long countByStatusAndDeletedFalse(String status);

    long countByDepartmentAndDeletedFalse(String department);
}