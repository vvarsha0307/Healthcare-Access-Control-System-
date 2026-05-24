package com.example.demo.security.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SessionService {

    // Idle timeout — 30 minutes
    private static final int IDLE_MINUTES = 30;

    @Autowired
    private UserSessionRepository repo;

    // Invalidate single token
    public void invalidateToken(String token) {
        repo.findFirstByTokenAndActiveTrueOrderByIdDesc(token)
                .ifPresent(s -> {
                    s.setActive(false);
                    repo.save(s);
                });
    }

    // Invalidate all sessions for a user
    public void invalidateAll(Long userId) {
        List<UserSession> sessions = repo.findByUserIdAndActiveTrue(userId);
        sessions.forEach(s -> s.setActive(false));
        repo.saveAll(sessions);
    }

    // Check if session has been idle too long
    public boolean isIdleExpired(UserSession s) {
        return s.getLastActivity()
                .isBefore(LocalDateTime.now().minusMinutes(IDLE_MINUTES));
    }

    // Refresh last activity timestamp
    public void updateActivity(UserSession s) {
        s.setLastActivity(LocalDateTime.now());
        repo.save(s);
    }
}