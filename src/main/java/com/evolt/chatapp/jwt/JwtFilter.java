package com.evolt.chatapp.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    private static final List<String> PUBLIC_ROUTES = List.of(
            "/auth",
            "/error"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1. PUBLIC rute (bez auth)
        boolean isPublic = PUBLIC_ROUTES.stream()
                .anyMatch(path::startsWith);

        if (isPublic) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. uzmi token iz cookie
        String token = null;

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("token".equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }

        // 3. nema token → 401
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            // 4. validacija tokena
            String userId = jwtService.extractUserId(token);

            // možeš dodati i username ako hoćeš
            request.setAttribute("userId", userId);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 5. nastavi dalje
        filterChain.doFilter(request, response);
    }
}