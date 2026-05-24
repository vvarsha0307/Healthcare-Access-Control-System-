package com.example.demo.security.rules;
import com.example.demo.security.session.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class RuleEngineService {
    @Autowired
    private SessionService sessionService;
    // ===============================
    // 🔴 NURSE RULES
    // ===============================
    // Nurse approving without doctor sign
    public boolean nurseApproveCheck(
            String token,
            boolean doctorSigned) {
        if (!doctorSigned) {
            sessionService.invalidateToken(token);
            return false;
        }
        return true;
    }
    // Frequent vitals update
    public boolean nurseVitalsCheck(
            String token,
            int updatesCount) {
        // example: max 5 updates
        if (updatesCount > 5) {
            sessionService.invalidateToken(token);
            return false;
        }
        return true;
    }
    // ===============================
    // 🔵 DOCTOR RULES
    // ===============================
    // Intent mismatch
    public boolean doctorIntentCheck(
            String token,
            boolean allowedIntent) {
        if (!allowedIntent) {
            sessionService.invalidateToken(token);
            return false;
        }
        return true;
    }
    // Anchor event skip
    public boolean doctorAnchorCheck(
            String token,
            boolean anchorValid) {
        if (!anchorValid) {
            sessionService.invalidateToken(token);
            return false;
        }
        return true;
    }
    // =======================================
// DOCTOR SIGN RULE (ANCHOR + INTENT)
// =======================================
    public boolean doctorSignCheck(
            String token,
            boolean anchorValid,
            boolean intentAllowed) {
        if (!anchorValid || !intentAllowed) {

            sessionService.invalidateToken(token);
            return false;
        }
        return true;
    }
}

