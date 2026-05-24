package com.example.demo.repository;

import com.example.demo.entity.UserShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserShiftRepository extends JpaRepository<UserShift, Long> {

    // ✅ FIXED: Returns UserShift directly — NOT Optional<UserShift>
    Optional<UserShift> findTopByUserIdOrderByIdDesc(Long userId);

    // ✅ KEEP: Used in ShiftAccessService.getActiveShiftName() and isNurseWithinShift()
    List<UserShift> findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long userId, LocalDate start, LocalDate end);

    // ✅ KEEP: Get all shifts for user (for delete/update)
    List<UserShift> findByUserId(Long userId);
}