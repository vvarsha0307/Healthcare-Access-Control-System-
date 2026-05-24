package com.example.demo.controller;

import com.example.demo.DTO.LoginRequest;
import com.example.demo.DTO.LoginResponse;
import com.example.demo.DTO.MainLoginRequest;
import com.example.demo.DTO.RegisterRequest;
import com.example.demo.model.AccessLog;
import com.example.demo.model.User;
import com.example.demo.security.JwtUtil;
import com.example.demo.security.session.UserSession;
import com.example.demo.security.session.UserSessionRepository;
import com.example.demo.service.ShiftAccessService;
import com.example.demo.service.UserService;
import com.example.demo.util.GeoUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ShiftAccessService shiftAccessService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserSessionRepository sessionRepo;
    private final GeoUtils geoUtils;

    public AuthController(UserService userService,
                          JwtUtil jwtUtil,
                          UserSessionRepository sessionRepo,
                          GeoUtils geoUtils,
                          ShiftAccessService shiftAccessService) {
        this.userService         = userService;
        this.jwtUtil             = jwtUtil;
        this.sessionRepo         = sessionRepo;
        this.geoUtils            = geoUtils;
        this.shiftAccessService  = shiftAccessService;
    }

    // ── REGISTER ──────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            userService.registerUser(request);
            return ResponseEntity.ok(Map.of(
                    "message", "User Registered Successfully ✅",
                    "status",  "PENDING_APPROVAL"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── MAIN LOGIN ────────────────────────────────────────────
    @PostMapping("/main-login")
    public ResponseEntity<?> mainLogin(@Valid @RequestBody MainLoginRequest request) {
        User user = userService.mainLogin(request.getHospitalEmail(), request.getStaffId());
        if (user == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid Hospital Credentials ❌"));
        return ResponseEntity.ok(Map.of("message", "Main Dashboard Access Granted ✅"));
    }

    // ── LOGIN — ORDER: Credentials → Shift → Location ─────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {

        String ip           = httpRequest.getRemoteAddr();
        boolean isLocalhost = ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1");
        boolean isHospitalLan = ip.startsWith("192.168.");

        // ── STEP 1: Network check (fast fail — no DB hit needed) ──
        if (!isLocalhost && !isHospitalLan) {
            return buildDeniedResponse(request, ip, null, null,
                    "Login blocked: Must be on hospital network ❌");
        }

        // ── STEP 2: Validate credentials ──────────────────────
        User user = userService.authenticateByUsername(request.getUsername(), request.getPassword());
        if (user == null) {
            return buildDeniedResponse(request, ip, null, null, "Invalid credentials ❌");
        }

        String role = user.getRole().getName().toUpperCase();

        // ── STEP 3: Admin approval check ──────────────────────
        if (!user.isApproved() && !role.equalsIgnoreCase("ADMIN")) {
            return buildDeniedResponse(request, ip, user.getUserId(), role,
                    "Waiting for admin approval ❌");
        }

        // ── STEP 4: Shift check (doctors and nurses only) ─────
        boolean requireShift = role.equalsIgnoreCase("DOCTOR") || role.equalsIgnoreCase("NURSE");
        if (requireShift && !shiftAccessService.isUserAllowedToLogin(user.getUserId())) {
            return buildDeniedResponse(request, ip, user.getUserId(), role,
                    "Not your shift time ❌");
        }

        // ── STEP 5: Location check — ALWAYS enforced ─────────
        // ADMIN and RECEPTIONIST exempt (office-based roles)
        // Everyone else (DOCTOR, NURSE) must be within hospital campus
        boolean locationExempt = role.equalsIgnoreCase("ADMIN")
                || role.equalsIgnoreCase("RECEPTIONIST");

        if (!locationExempt) {
            double lat = request.getLatitude();
            double lng = request.getLongitude();

            // Block if no location sent (browser denied permission)
            if (lat == 0 && lng == 0) {
                return buildDeniedResponse(request, ip, user.getUserId(), role,
                        "Location required — please allow location access in your browser ❌");
            }

            // Block if outside hospital campus
            if (!geoUtils.isWithinHospital(lat, lng)) {
                return buildDeniedResponse(request, ip, user.getUserId(), role,
                        "Access Denied: You are outside the hospital campus ❌");
            }
        }

        // ── STEP 6: Issue token & create session ──────────────
        // Deactivate old sessions for doctors (single session policy)
        if (role.equalsIgnoreCase("DOCTOR")) {
            sessionRepo.deactivateAllSessionsForUser(user.getUserId());
        }

        String token = jwtUtil.generateToken(user.getUsername(), role);

        UserSession session = new UserSession();
        session.setUserId(user.getUserId());
        session.setToken(token);
        session.setRole(role);
        session.setIpAddress(ip);
        session.setLastActivity(LocalDateTime.now());
        session.setActive(true);
        session.setDepartment(user.getDepartment());
        sessionRepo.save(session);

        saveAccessLog(request, ip, user.getUserId(), role, "ALLOWED", "Login successful ✅");

        return ResponseEntity.ok(new LoginResponse(
                token, role, "Login successful ✅",
                user.getUsername(), user.getSubRole()
        ));
    }

    // ── LOGOUT ────────────────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            sessionRepo.findFirstByTokenAndActiveTrueOrderByIdDesc(token).ifPresent(session -> {
                session.setActive(false);
                sessionRepo.save(session);
            });
            return ResponseEntity.ok(Map.of("message", "Logged out successfully ✅"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid token ❌"));
    }

    // ── VALIDATE INTENT ───────────────────────────────────────
    @PostMapping("/validate-intent")
    public ResponseEntity<?> validateIntent(@RequestBody Map<String, String> payload,
                                            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7) : null;
        if (token == null)
            return ResponseEntity.status(403).body(Map.of("error", "Invalid token ❌"));

        UserSession session = sessionRepo
                .findFirstByTokenAndActiveTrueOrderByIdDesc(token).orElse(null);
        if (session == null)
            return ResponseEntity.status(403).body(Map.of("error", "Session expired ❌"));

        String action     = payload.get("action");
        String department = payload.get("department");
        String lastAction = session.getLastAction();
        String userDept   = session.getDepartment();

        // Department check
        if (userDept != null && department != null
                && !userDept.equalsIgnoreCase(department)) {
            session.setActive(false);
            sessionRepo.save(session);
            return ResponseEntity.status(403).body(Map.of("error", "Department violation ❌"));
        }

        // ── Flow check ────────────────────────────────────────
        boolean valid = false;
        switch (action.toUpperCase()) {
            case "VIEW_VITALS":
                valid = lastAction == null || lastAction.equals("VIEW_VITALS");
                break;
            case "ENTER_DIAGNOSIS":
                valid = "VIEW_VITALS".equals(lastAction);
                break;
            case "ENTER_TREATMENT":
                valid = "ENTER_DIAGNOSIS".equals(lastAction);
                break;
            // ✅ FIXED: SIGN allowed after ENTER_DIAGNOSIS or ENTER_TREATMENT
            case "SIGN":
                valid = "ENTER_DIAGNOSIS".equals(lastAction)
                        || "ENTER_TREATMENT".equals(lastAction);
                break;
        }

        if (!valid) {
            session.setActive(false);
            sessionRepo.save(session);
            return ResponseEntity.status(403).body(Map.of("error", "Intent policy violated ❌"));
        }

        session.setLastAction(action.toUpperCase());
        sessionRepo.save(session);

        return ResponseEntity.ok(Map.of("message", "Action allowed ✅"));
    }

    // ── HELPERS ───────────────────────────────────────────────
    private ResponseEntity<Map<String, String>> buildDeniedResponse(LoginRequest request,
                                                                    String ip,
                                                                    Long userId,
                                                                    String role,
                                                                    String message) {
        saveAccessLog(request, ip, userId, role, "DENIED", message);
        return ResponseEntity.status(403).body(Map.of("error", message));
    }

    private void saveAccessLog(LoginRequest request, String ip,
                               Long userId, String role,
                               String status, String message) {
        try {
            AccessLog log = new AccessLog();
            log.setUsername(request.getUsername());
            log.setIpAddress(ip);
            log.setLatitude(request.getLatitude());
            log.setLongitude(request.getLongitude());
            log.setUserId(userId);
            log.setRole(role);
            log.setResult(status);
            log.setStatus(status);
            log.setMessage(message);
            log.setTimestamp(LocalDateTime.now());
            userService.saveAccessLog(log);
        } catch (Exception e) {
            System.err.println("⚠️ Access log save failed: " + e.getMessage());
        }
    }
}