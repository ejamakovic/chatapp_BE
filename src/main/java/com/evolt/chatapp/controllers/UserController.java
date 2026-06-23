package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.UserDto;
import com.evolt.chatapp.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * List all users — ADMIN only.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> allUsers() {
        return userService.getAllUsers()
                .stream()
                .map(UserDto::new)
                .toList();
    }

    /**
     * List currently connected users — any authenticated user.
     */
    @GetMapping("/connected")
    public List<UserDto> connectedUsers() {
        return userService.getAllConnectedUsers()
                .stream()
                .map(UserDto::new)
                .toList();
    }

    /**
     * Get own profile — any authenticated user.
     * A user can only fetch their own profile unless they are ADMIN.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String requesterId = (String) request.getAttribute("userId");
        if (!isAdminOrSelf(requesterId, id, request)) {
            return ResponseEntity.status(403).build();
        }
        User user = userService.findById(id);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new UserDto(user));
    }

    /**
     * Upload or replace the current user's avatar.
     * Users can only update their own avatar; admins can update any.
     */
    @PatchMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(
            @PathVariable  Long          id,
            @RequestParam  MultipartFile file,
            HttpServletRequest           request
    ) {
        String requesterId = (String) request.getAttribute("userId");
        if (!isAdminOrSelf(requesterId, id, request)) {
            return ResponseEntity.status(403).build();
        }
        try {
            String url = userService.updateAvatar(id, file);
            return ResponseEntity.ok(new AvatarResponse(url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to save avatar");
        }
    }

    // ── Small inline response record ──────────────────────────────────────────

    public record AvatarResponse(String avatarUrl) {}

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isAdminOrSelf(String requesterId, Long targetId, HttpServletRequest request) {
        if (requesterId == null) return false;
        if (Long.valueOf(requesterId).equals(targetId)) return true;
        // Check if the caller has ADMIN role via Spring Security context
        return request.isUserInRole("ROLE_ADMIN");
    }
}