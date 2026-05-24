package com.example.demo.service;

import com.example.demo.entity.UserShift;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserShiftRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ShiftAccessService {

    private final UserShiftRepository userShiftRepository;
    private final UserRepository userRepository;

    public ShiftAccessService(UserShiftRepository userShiftRepository,
                              UserRepository userRepository) {
        this.userShiftRepository = userShiftRepository;
        this.userRepository      = userRepository;
    }

    /** Called at LOGIN from AuthController */
    public boolean isUserAllowedToLogin(Long userId) {
        return checkShiftWindow(userId, "LOGIN");
    }

    /** Called on EVERY REQUEST from NurseController */
    public boolean isUserWithinShift(Long userId) {
        return checkShiftWindow(userId, "REQUEST");
    }

    /** Shared date+time window check — applies to ALL nurses including HEAD_NURSE */
    private boolean checkShiftWindow(Long userId, String context) {
        LocalDate today = LocalDate.now();
        LocalTime now   = LocalTime.now();

        List<UserShift> shifts =
                userShiftRepository
                        .findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                userId, today, today);

        if (shifts == null || shifts.isEmpty()) {
            System.out.println("❌ [" + context + "] No shift record for userId="
                    + userId + " on " + today);
            return false;
        }

        for (UserShift shift : shifts) {
            if (shift.getShiftName() == null || shift.getShiftName().isBlank()) {
                System.out.println("⚠️ Null/blank shiftName for userId=" + userId);
                continue;
            }
            String name = shift.getShiftName().trim().toUpperCase();
            boolean inWindow = isWithinShiftTime(name, now);
            System.out.println("🕐 [" + context + "] userId=" + userId
                    + " shift=" + name + " now=" + now
                    + " → " + (inWindow ? "ALLOWED" : "BLOCKED"));
            if (inWindow) return true;
        }

        System.out.println("❌ [" + context + "] userId=" + userId
                + " blocked — no matching shift window at " + now);
        return false;
    }

    /** For dashboard display */
    public String getActiveShiftName(Long userId) {
        LocalDate today = LocalDate.now();
        LocalTime now   = LocalTime.now();

        List<UserShift> shifts =
                userShiftRepository
                        .findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                userId, today, today);

        if (shifts == null || shifts.isEmpty()) return "No Shift";

        for (UserShift shift : shifts) {
            if (shift.getShiftName() == null || shift.getShiftName().isBlank()) continue;
            String name = shift.getShiftName().trim().toUpperCase();
            if (isWithinShiftTime(name, now)) return name;
        }

        return "Outside Shift";
    }

    private boolean isWithinShiftTime(String shiftName, LocalTime now) {
        return switch (shiftName) {
            case "MORNING" ->
                    !now.isBefore(LocalTime.of(8, 0)) && now.isBefore(LocalTime.of(14, 0));
            case "EVENING" ->
                    !now.isBefore(LocalTime.of(14, 0)) && now.isBefore(LocalTime.of(20, 0));
            case "NIGHT" ->
                    !now.isBefore(LocalTime.of(20, 0)) || now.isBefore(LocalTime.of(8, 0));
            default -> {
                System.out.println("⚠️ Unknown shiftName: '" + shiftName + "'");
                yield false;
            }
        };
    }
}