package com.evolt.chatapp.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    /**
     * Injected from application.properties / environment variable.
     * Must be at least 256-bit (32 ASCII chars) for HS256.
     */
    private final Key secretKey;

    /** Access token lifetime in milliseconds. Default: 15 minutes. */
    private final long accessTokenMs;

    /** Refresh token lifetime in milliseconds. Default: 7 days. */
    private final long refreshTokenMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry-ms:900000}") long accessTokenMs,
            @Value("${jwt.refresh-token-expiry-ms:604800000}") long refreshTokenMs
    ) {
        if (secret.length() < 32) {
            throw new IllegalArgumentException(
                    "jwt.secret must be at least 32 characters for HS256");
        }
        this.secretKey      = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenMs  = accessTokenMs;
        this.refreshTokenMs = refreshTokenMs;
    }

    // ── Token generation ──────────────────────────────────────────────────────

    public String generateAccessToken(Long userId, String username, String role) {
        return buildToken(userId, username, role, accessTokenMs);
    }

    /**
     * Refresh token carries minimal claims — just the subject (userId).
     * It is stored as an HttpOnly cookie and exchanged for a new access token.
     */
    public String generateRefreshToken(Long userId, String username, String role) {
        return buildToken(userId, username, role, refreshTokenMs);
    }

    private String buildToken(Long userId, String username, String role, long ttlMs) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + ttlMs);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("role",     role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    // ── Token parsing ─────────────────────────────────────────────────────────

    /**
     * Returns parsed claims or throws {@link JwtException} / {@link IllegalArgumentException}
     * if the token is invalid or expired.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractUsername(String token) {
        return (String) extractAllClaims(token).get("username");
    }

    public String extractRole(String token) {
        return (String) extractAllClaims(token).get("role");
    }

    /** Returns true when the token is structurally valid and not expired. */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);   // throws if expired or tampered
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}