package com.example.demo.security.session;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "session_table")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String token;
    private String role;

    @Column(name = "ip_address")
    private String ipAddress;

    private boolean active = true;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    // NEW FIELDS
    private String lastAction;   // last workflow step
    private String department;   // doctor department

    // ===== GETTERS & SETTERS =====
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    public String getLastAction() { return lastAction; }
    public void setLastAction(String lastAction) { this.lastAction = lastAction; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}