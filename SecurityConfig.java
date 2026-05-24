package com.example.demo.security;

import com.example.demo.security.session.JwtSessionFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtSessionFilter jwtSessionFilter;

    public SecurityConfig(JwtSessionFilter jwtSessionFilter) {
        this.jwtSessionFilter = jwtSessionFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()

                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/doctor/**").hasAuthority("ROLE_DOCTOR")

                        // ✅ KEY FIX: JWT filter does "ROLE_" + role.toUpperCase()
                        // Dept nurse  → role="NURSE"      → ROLE_NURSE
                        // Head nurse  → role="NURSE"      → ROLE_NURSE  (subRole is separate)
                        // Both use same role field in JWT — subRole is only in localStorage
                        .requestMatchers("/nurse/**")
                        .hasAnyAuthority("ROLE_NURSE", "ROLE_HEAD_NURSE")

                        .requestMatchers("/reception/**").hasAuthority("ROLE_RECEPTIONIST")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtSessionFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}