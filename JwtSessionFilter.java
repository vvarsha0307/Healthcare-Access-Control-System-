package com.example.demo.security.session;

import com.example.demo.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// ✅ FIXED: Only ONE JwtSessionFilter — kept in session package
// Delete any other JwtSessionFilter in security/filter/ package if it exists
@Component
public class JwtSessionFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserSessionRepository sessionRepo;
    private final SessionService sessionService;

    public JwtSessionFilter(JwtUtil jwtUtil,
                            UserSessionRepository sessionRepo,
                            SessionService sessionService) {
        this.jwtUtil       = jwtUtil;
        this.sessionRepo   = sessionRepo;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // ✅ Allow public auth endpoints
        if (path.startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized ❌");
            return;
        }

        String token = authHeader.substring(7);

        try {
            // ✅ Validate JWT signature & expiry first
            if (jwtUtil.isTokenExpired(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token expired ❌");
                return;
            }

            Claims claims   = jwtUtil.extractClaims(token);
            String username = claims.getSubject();
            String role     = claims.get("role", String.class);

            // ✅ Validate active session in DB
            UserSession session = sessionRepo
                    .findFirstByTokenAndActiveTrueOrderByIdDesc(token)
                    .orElse(null);

            if (session == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid session ❌");
                return;
            }

            // ✅ Check idle timeout (15 min)
            if (sessionService.isIdleExpired(session)) {
                sessionService.invalidateToken(token);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Session expired ❌");
                return;
            }

            // ✅ Update last activity
            sessionService.updateActivity(session);

            // ✅ Set Spring Security authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token ❌");
        }
    }
}