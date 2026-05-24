package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "intent_policy")
public class IntentPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long policy_id;

    private String role;
    private String department;
    private String intent;

    @Column(name = "allowed_data")
    private String allowedData;

    // ===== GETTERS =====

    public Long getPolicy_id() {
        return policy_id;
    }

    public String getRole() {
        return role;
    }

    public String getDepartment() {
        return department;
    }

    public String getIntent() {
        return intent;
    }

    public String getAllowedData() {
        return allowedData;
    }

    // ===== SETTERS =====

    public void setPolicy_id(Long policy_id) {
        this.policy_id = policy_id;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public void setAllowedData(String allowedData) {
        this.allowedData = allowedData;
    }
}