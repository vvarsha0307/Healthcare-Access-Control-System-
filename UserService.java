package com.example.demo.service;

import com.example.demo.DTO.RegisterRequest;
import com.example.demo.entity.UserShift;
import com.example.demo.model.AccessLog;
import com.example.demo.model.ActivityLog;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccessLogRepository accessLogRepo;
    private final ActivityLogRepository activityLogRepo;

    @Autowired
    private UserShiftRepository userShiftRepository;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       AccessLogRepository accessLogRepo,
                       ActivityLogRepository activityLogRepo) {

        this.userRepository  = userRepository;
        this.roleRepository  = roleRepository;
        this.accessLogRepo   = accessLogRepo;
        this.activityLogRepo = activityLogRepo;
    }

    // =========================
    // GET PENDING USERS
    // =========================
    public List<User> getPendingUsers() {
        return userRepository.findByApprovedFalse();
    }

    // =========================
    // REGISTER USER
    // =========================
    public void registerUser(RegisterRequest request) {

        // =========================
        // DUPLICATE CHECK
        // =========================
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists ❌");
        }

        if (userRepository.existsByEmailId(request.getEmailId())) {
            throw new RuntimeException("Email already exists ❌");
        }

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmailId(request.getEmailId());

        user.setPassword(
                com.example.demo.security.Argon2Util
                        .hashPassword(request.getPassword())
        );

        // =========================
        // ROLE
        // =========================
        Role role = roleRepository.findByName(request.getRole().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Invalid role ❌"));

        user.setRole(role);

        // =========================
        // DEPARTMENT
        // =========================
        if (!role.getName().equalsIgnoreCase("ADMIN")) {

            if (request.getDepartment() == null || request.getDepartment().isBlank()) {
                throw new RuntimeException("Department required ❌");
            }

            user.setDepartment(request.getDepartment());

        } else {
            user.setDepartment(null);
        }

        // =========================
        // SUB ROLE (NURSE)
        // =========================
        if (role.getName().equalsIgnoreCase("NURSE")) {

            if (request.getSubRole() == null || request.getSubRole().isBlank()) {
                throw new RuntimeException("SubRole required ❌");
            }

            user.setSubRole(request.getSubRole());

        } else {
            user.setSubRole(null);
        }

        // =========================
        // 🔥 IMPORTANT FIX
        // =========================
        user.setApproved(false);          // ❌ NOT APPROVED
        user.setStatus("INACTIVE");       // ❌ NOT ACTIVE

        // ADMIN should not register normally
        if (role.getName().equalsIgnoreCase("ADMIN")) {
            user.setApproved(true);
            user.setStatus("ACTIVE");
        }

        userRepository.save(user);
    }
    // =========================
    // 🔥 FIXED: ASSIGN SHIFT
    // =========================
    public void assignShift(Long userId, String shiftName) {

        User user = findById(userId);
        if (user == null) return;

        String role = user.getRole().getName();

        // ❌ DO NOT ASSIGN SHIFT FOR ADMIN / RECEPTIONIST
        if (role.equalsIgnoreCase("ADMIN") ||
                role.equalsIgnoreCase("RECEPTIONIST")) {
            return;
        }

        // ✅ REMOVE OLD SHIFTS
        List<UserShift> existing = userShiftRepository.findAll();
        existing.stream()
                .filter(s -> s.getUserId().equals(userId))
                .forEach(userShiftRepository::delete);

        UserShift shift = new UserShift();

        shift.setUserId(userId);
        shift.setShiftName(shiftName.toUpperCase());

        int offset = 0;
        if (shiftName.equalsIgnoreCase("EVENING")) offset = 10;
        else if (shiftName.equalsIgnoreCase("NIGHT")) offset = 20;

        shift.setStartDate(LocalDate.now().minusDays(offset));
        shift.setEndDate(LocalDate.now().plusMonths(3));

        userShiftRepository.save(shift);
    }

    // =========================
    // LOGIN
    // =========================
    public User authenticateByUsername(String username, String password) {

        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) return null;

        if (!com.example.demo.security.Argon2Util
                .verifyPassword(user.getPassword(), password))
            return null;

        return user;
    }
    // =========================
// 🔥 FIX: MAIN LOGIN METHOD (ADD THIS BACK)
// =========================
    public User mainLogin(String email, String password) {

        User user = userRepository.findByEmailId(email).orElse(null);

        if (user == null) return null;

        if (!com.example.demo.security.Argon2Util
                .verifyPassword(user.getPassword(), password)) {
            return null;
        }

        return user;
    }
    // =========================
    // ACCESS LOG
    // =========================
    public void saveAccessLog(AccessLog log) {
        accessLogRepo.save(log);
    }

    // =========================
    // ADMIN LOG
    // =========================
    public void logAdminActivity(String adminUsername, String action) {

        ActivityLog log = new ActivityLog();
        log.setUsername(adminUsername);
        log.setRole("ADMIN");
        log.setAction(action);
        log.setResult("SUCCESS");
        log.setTimestamp(LocalDateTime.now());

        activityLogRepo.save(log);
    }

    public List<ActivityLog> getAllActivityLogs() {
        return activityLogRepo.findAllByOrderByTimestampDesc();
    }

    // =========================
    // USERS
    // =========================
    public List<User> getAllActiveUsers() {
        return userRepository.findByDeletedFalse();
    }

    public List<User> getDeletedUsers() {
        return userRepository.findByDeletedTrue();
    }

    public User activateUser(Long id, String adminUsername) {

        User user = findById(id);
        if (user == null) return null;

        user.setStatus("ACTIVE");
        userRepository.save(user);

        logAdminActivity(adminUsername, "Activated user: " + user.getUsername());

        return user;
    }

    public User deactivateUser(Long id, String adminUsername) {

        User user = findById(id);
        if (user == null) return null;

        user.setStatus("INACTIVE");
        userRepository.save(user);

        logAdminActivity(adminUsername, "Deactivated user: " + user.getUsername());

        return user;
    }

    public String softDeleteUser(Long id, String adminUsername) {

        User user = findById(id);
        if (user == null) return "User not found ❌";

        user.setDeleted(true);
        userRepository.save(user);

        logAdminActivity(adminUsername, "Deleted user: " + user.getUsername());

        return "User moved to Deleted Users ✅";
    }

    public boolean staffIdExists(String staffId) {
        return userRepository.existsByStaffId(staffId);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public void save(User user) {
        userRepository.save(user);
    }
    // =========================
// 🔥 FIND BY USERNAME (REQUIRED)
// =========================
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}