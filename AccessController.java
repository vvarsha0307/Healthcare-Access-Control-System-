package com.example.demo.controller;
import com.example.demo.model.AccessRequest;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/access")
public class AccessController {
    @GetMapping("/hello")
    public String hello() {
        return "Backend is working ✅";
    }
    @PostMapping("/check")
    public String checkAccess(@RequestBody AccessRequest request) {
        if (request.getRole().equals("DOCTOR")
                && request.getDepartment().equals("CARDIOLOGY")
                && request.getReportType().equals("HEART_REPORT")) {
            return "ACCESS GRANTED";
        }
        return "ACCESS DENIED";
    }
}
