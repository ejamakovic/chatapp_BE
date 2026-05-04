package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.User;
import com.evolt.chatapp.services.JwtService;
import com.evolt.chatapp.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, HttpServletResponse response) {
        User user = userService.createIfNotExists(username);

        String token = jwtService.generateToken(
                String.valueOf(user.getId()),
                user.getUsername()
        );

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@CookieValue("token") String token) {

        String userId = jwtService.extractUserId(token);
        User user = userService.findById(Long.valueOf(userId));

        return ResponseEntity.ok(user);
    }
}