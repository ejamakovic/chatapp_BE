package com.evolt.chatapp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory sliding-window rate limiter for the auth endpoints,
 * to slow down brute-force login/registration attempts.
 * NOTE: per-instance memory only — fine for one backend node, won't
 * coordinate across multiple instances behind a load balancer (move to
 * Redis or similar if you scale horizontally).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_WINDOW = 10;
    private static final long WINDOW_MS = 60_000;

    private static final Set<String> LIMITED_PATHS = Set.of(
            "/auth/login",
            "/auth/register"
    );

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static class Bucket {
        volatile long windowStart = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger(0);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (LIMITED_PATHS.contains(path)) {
            String key = clientIp(request) + ":" + path;
            Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket());

            boolean limited;
            synchronized (bucket) {
                long now = System.currentTimeMillis();
                if (now - bucket.windowStart > WINDOW_MS) {
                    bucket.windowStart = now;
                    bucket.count.set(0);
                }
                limited = bucket.count.incrementAndGet() > MAX_REQUESTS_PER_WINDOW;
            }

            if (limited) {
                response.setStatus(429);
                response.setContentType("text/plain");
                response.getWriter().write("Too many requests. Please try again in a minute.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}