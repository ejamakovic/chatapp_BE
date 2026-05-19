package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.User;
import com.evolt.chatapp.jwt.JwtService;
import com.evolt.chatapp.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private JwtService jwtService;

    private UserService userService;

    private AuthController(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username,
                                   HttpServletResponse response) {

        User user = userService.createIfNotExists(username);

        String token = jwtService.generateToken(
                String.valueOf(user.getId()),
                user.getUsername()
        );

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok(
                Map.of(
                        "token", token,
                        "user", user
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@CookieValue("token") String token) {

        String userId = jwtService.extractUserId(token);
        User user = userService.findById(Long.valueOf(userId));

        return ResponseEntity.ok(
                java.util.Map.of(
                        "token", token,
                        "user", user
                )
        );
    }
}