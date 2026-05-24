package com.example.demo.DTO;
public class LoginResponse {
    private String token;
    private String role;
    private String message;
    private String username;
    private String subRole;
    public LoginResponse(String token, String role, String message,String username,String subRole ) {
        this.token = token;
        this.role = role;
        this.message = message;
        this.username = username;
        this.subRole = subRole;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getSubRole() {
        return subRole;
    }
    public void setSubRole(String subRole) {
        this.subRole = subRole;
    }
}

