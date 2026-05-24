package com.example.demo.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController
@RequestMapping("/users")
public class UserController {
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER')")
    public String userDashboard() {
        return "Welcome User Dashboard";
    }
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public String userProfile() {
        return "User Profile Details";
    }
    @GetMapping("/settings")
    @PreAuthorize("hasRole('USER')")
    public String userSettings() {
        return "User Settings Page";
    }
}
