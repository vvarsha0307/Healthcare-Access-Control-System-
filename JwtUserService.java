package com.example.demo.security;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JwtUserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    // =========================
    // COMMON METHOD
    // =========================
    private User getUserFromToken(String authHeader) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("❌ Invalid Authorization header");
                return null;
            }

            String token = authHeader.substring(7);

            // ✅ Extract claims safely
            Claims claims = jwtUtil.extractClaims(token);

            String username = claims.getSubject();

            System.out.println("JWT Username: " + username);

            return userRepository.findByUsername(username).orElse(null);

        } catch (Exception e) {
            System.out.println("❌ JWT ERROR: " + e.getMessage());
            return null;
        }
    }

    // =========================
    // GET USER ID
    // =========================
    public Long extractUserId(String authHeader) {

        User user = getUserFromToken(authHeader);

        if (user == null) {
            System.out.println("❌ User not found from token");
            return null;
        }

        System.out.println("✅ User ID: " + user.getUserId());

        return user.getUserId();
    }

    // =========================
    // GET DEPARTMENT
    // =========================
    public String extractDepartment(String authHeader) {

        User user = getUserFromToken(authHeader);

        return user != null ? user.getDepartment() : null;
    }

    // =========================
    // GET ROLE
    // =========================
    public String extractRole(String authHeader) {

        User user = getUserFromToken(authHeader);

        return (user != null && user.getRole() != null)
                ? user.getRole().getName()
                : null;
    }
}