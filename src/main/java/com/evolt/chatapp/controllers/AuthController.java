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
import jakarta.validation.Valid;
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

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest body,
            HttpServletResponse response
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

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest body,
            HttpServletResponse response
    ) {
        User user = userService.findByUsername(body.getUsername());

        if (user == null || !userService.checkPassword(user, body.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }

        return ResponseEntity.ok(buildAuthResponse(user, response));
    }

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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest  request,
            HttpServletResponse response
    ) {
        Cookie clear = new Cookie("refreshToken", "");
        clear.setMaxAge(0);
        clear.setHttpOnly(true);
        clear.setPath("/");
        response.addCookie(clear);

        String userId = (String) request.getAttribute("userId");
        if (userId != null) {
            userService.setConnected(Long.valueOf(userId), false);
        }

        return ResponseEntity.ok().build();
    }

    private AuthResponse buildAuthResponse(User user, HttpServletResponse response) {
        String role         = user.getRole().name();
        String accessToken  = jwtService.generateAccessToken(user.getId(), user.getUsername(), role);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getUsername(), role);

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);

        return new AuthResponse(accessToken, refreshToken, new UserDto(user));
    }
}