package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.AuthResponse;
import com.evolt.chatapp.models.dto.LoginRequest;
import com.evolt.chatapp.models.dto.RegisterRequest;
import com.evolt.chatapp.models.dto.UserDto;
import com.evolt.chatapp.jwt.JwtService;
import com.evolt.chatapp.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService  jwtService;
    private final UserService userService;

    public AuthController(JwtService jwtService, UserService userService) {
        this.jwtService  = jwtService;
        this.userService = userService;
    }

    // ── POST /auth/register ───────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody  RegisterRequest  body,
            HttpServletResponse            response
    ) {
        try {
            User user = userService.register(body);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(buildAuthResponse(user, response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── POST /auth/login ──────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody  LoginRequest     body,
            HttpServletResponse            response
    ) {
        User user = userService.findByUsername(body.getUsername());

        if (user == null || !userService.checkPassword(user, body.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }

        return ResponseEntity.ok(buildAuthResponse(user, response));
    }

    // ── POST /auth/refresh ────────────────────────────────────────────────────

    /**
     * Accepts the refresh token from the HttpOnly cookie and issues a new access token.
     * The refresh token itself is also rotated (old one invalidated by issuing a new one).
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null || !jwtService.isTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing refresh token");
        }

        String userId  = jwtService.extractUserId(refreshToken);
        User   user    = userService.findById(Long.valueOf(userId));

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        return ResponseEntity.ok(buildAuthResponse(user, response));
    }

    // ── GET /auth/me ──────────────────────────────────────────────────────────

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.findById(Long.valueOf(userId));
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(new UserDto(user));
    }

    // ── POST /auth/logout ─────────────────────────────────────────────────────

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest  request,
            HttpServletResponse response
    ) {
        // Clear the HttpOnly cookie on the client side
        Cookie clear = new Cookie("refreshToken", "");
        clear.setMaxAge(0);
        clear.setHttpOnly(true);
        clear.setPath("/");
        response.addCookie(clear);

        // Optionally mark user as offline
        String userId = (String) request.getAttribute("userId");
        if (userId != null) {
            userService.setConnected(Long.valueOf(userId), false);
        }

        return ResponseEntity.ok().build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user, HttpServletResponse response) {
        String role         = user.getRole().name();
        String accessToken  = jwtService.generateAccessToken(user.getId(), user.getUsername(), role);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getUsername(), role);

        // Refresh token in HttpOnly cookie — JS cannot read it
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);       // send only over HTTPS in production
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(7 * 24 * 60 * 60);   // 7 days
        response.addCookie(cookie);

        return new AuthResponse(accessToken, refreshToken, new UserDto(user));
    }
}