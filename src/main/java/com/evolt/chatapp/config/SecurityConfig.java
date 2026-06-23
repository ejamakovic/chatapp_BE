package com.evolt.chatapp.config;

import com.evolt.chatapp.jwt.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize on controller methods
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Work-factor 12 is a good balance of security vs. latency (≈ 300 ms on modern HW).
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ── Cross-origin ─────────────────────────────────────────────────
                .cors(cors -> {})           // delegate to CorsConfig bean
                .csrf(AbstractHttpConfigurer::disable)   // stateless JWT → no CSRF needed

                // ── Session ───────────────────────────────────────────────────────
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ── Route-level authorization ─────────────────────────────────────
                .authorizeHttpRequests(auth -> auth

                        // Public auth endpoints
                        .requestMatchers("/auth/login", "/auth/register", "/auth/refresh").permitAll()

                        // Static file serving (avatar uploads, etc.)
                        .requestMatchers("/uploads/**").permitAll()

                        // WebSocket upgrade — auth is handled by WebSocketAuthInterceptor
                        .requestMatchers("/ws/**").permitAll()

                        // Admin-only management routes
                        .requestMatchers(HttpMethod.DELETE, "/messages/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/users").hasRole("ADMIN")

                        // Everything else requires a valid (any role) JWT
                        .anyRequest().authenticated()
                )

                // ── JWT filter before Spring's own auth filter ────────────────────
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}