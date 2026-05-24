package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.security.JwtUserService;
import com.example.demo.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserService userService;
    private final JwtUserService jwtUserService;

    public AdminController(UserService userService,
                           JwtUserService jwtUserService) {
        this.userService = userService;
        this.jwtUserService = jwtUserService;
    }

    // =========================
    // 🔐 ADMIN CHECK
    // =========================
    private boolean isAdmin(HttpServletRequest request) {
        String role = jwtUserService.extractRole(request.getHeader("Authorization"));
        return role != null && role.equalsIgnoreCase("ADMIN");
    }

    // =========================
    // DASHBOARD
    // =========================
    @GetMapping("/dashboard")
    public ResponseEntity<?> adminDashboard(HttpServletRequest request) {

        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body("❌ Admin only");
        }

        return ResponseEntity.ok("Welcome Admin 👑");
    }

    // =========================
    // GET USERS
    // =========================
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {

        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body("❌ Admin only");
        }

        return ResponseEntity.ok(userService.getAllActiveUsers());
    }

    @GetMapping("/deleted-users")
    public ResponseEntity<?> getDeletedUsers(HttpServletRequest request) {

        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body("❌ Admin only");
        }

        return ResponseEntity.ok(userService.getDeletedUsers());
    }

    // =========================
    // ✅ APPROVE USER + SHIFT
    // =========================
    @PutMapping("/approve/{userId}/{staffId}/{shiftName}")
    public ResponseEntity<?> approveUser(@PathVariable Long userId,
                                         @PathVariable String staffId,
                                         @PathVariable String shiftName,
                                         HttpServletRequest request) {

        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body("❌ Admin only");
        }

        // =========================
        // VALIDATION
        // =========================
        if (userService.staffIdExists(staffId)) {
            return ResponseEntity.badRequest().body("Staff ID already exists ❌");
        }

        User user = userService.findById(userId);

        if (user == null) {
            return ResponseEntity.badRequest().body("User not found ❌");
        }

        // =========================
        // 🔥 FIXED FLOW
        // =========================
        user.setStaffId(staffId);
        user.setApproved(true);
        user.setStatus("ACTIVE");   // ✅ ONLY AFTER APPROVAL

        userService.save(user);

        // Assign shift
        userService.assignShift(userId, shiftName);

        userService.logAdminActivity("ADMIN",
                "Approved user: " + user.getUsername() +
                        " | staffId: " + staffId +
                        " | shift: " + shiftName);

        return ResponseEntity.ok("User approved + shift assigned ✅");
    }

    // =========================
    // DELETE USER
    // =========================
    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                        HttpServletRequest request) {

        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body("❌ Admin only");
        }

        return ResponseEntity.ok(userService.softDeleteUser(id, "ADMIN"));
    }

    // =========================
    // ACTIVATE USER
    // =========================
    @PutMapping("/user/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id,
                                          HttpServletRequest request) {

        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body("❌ Admin only");
        }

        User user = userService.activateUser(id, "ADMIN");

        return (user != null)
                ? ResponseEntity.ok("User Activated ✅")
                : ResponseEntity.badRequest().body("User not found ❌");
    }

    // =========================
    // DEACTIVATE USER
    // =========================
    @PutMapping("/user/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id,
                                            HttpServletRequest request) {

        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body("❌ Admin only");
        }

        User user = userService.deactivateUser(id, "ADMIN");

        return (user != null)
                ? ResponseEntity.ok("User Deactivated ✅")
                : ResponseEntity.badRequest().body("User not found ❌");
    }

    // =========================
    // ACTIVITY LOGS
    // =========================
    @GetMapping("/activity-logs")
    public ResponseEntity<?> getActivityLogs(HttpServletRequest request) {

        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body("❌ Admin only");
        }

        return ResponseEntity.ok(userService.getAllActivityLogs());
    }
}