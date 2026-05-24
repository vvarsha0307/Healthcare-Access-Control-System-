package com.example.demo.security.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    // =========================
    // GET ACTIVE SESSION BY TOKEN
    // =========================
    Optional<UserSession> findFirstByTokenAndActiveTrueOrderByIdDesc(String token);

    // =========================
    // GET ACTIVE SESSIONS BY USER
    // =========================
    List<UserSession> findByUserIdAndActiveTrue(Long userId);

    // =========================
    // GET ALL SESSIONS BY USER
    // =========================
    List<UserSession> findByUserId(Long userId);

    // =========================
    // 🔥 DEACTIVATE ALL SESSIONS (LOGIN FIX)
    // =========================
    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.active = false WHERE s.userId = :userId")
    void deactivateAllSessionsForUser(Long userId);

    // =========================
    // AUTO LOGOUT (DELETE)
    // =========================
    void deleteByUserId(Long userId);

    void deleteByToken(String token);
}