package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.UpdateProfileRequest;
import com.evolt.chatapp.models.dto.UserDto;
import com.evolt.chatapp.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
     * List all users. Opened up to any authenticated user (was ADMIN-only)
     * because the group-chat member picker needs it. Only ever returns
     * UserDto (no password), so this is a deliberate, contained relaxation.
     */
    @GetMapping
    public List<UserDto> allUsers() {
        return userService.getAllUsers()
                .stream()
                .map(UserDto::new)
                .toList();
    }

    @GetMapping("/connected")
    public List<UserDto> connectedUsers() {
        return userService.getAllConnectedUsers()
                .stream()
                .map(UserDto::new)
                .toList();
    }

    /** Own/admin-only profile fetch — includes email. */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String requesterId = (String) request.getAttribute("userId");
        if (isAdminOrSelf(requesterId, id, request)) {
            return ResponseEntity.status(403).build();
        }
        User user = userService.findById(id);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new UserDto(user));
    }

    /**
     * Public profile fetch — any authenticated user can view any profile.
     * Email is stripped unless viewing your own profile, or you're an admin.
     */
    @GetMapping("/{id}/profile")
    public ResponseEntity<UserDto> getProfile(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Long requesterId = Long.parseLong((String) request.getAttribute("userId"));
        boolean isAdmin = request.isUserInRole("ROLE_ADMIN");

        UserDto dto = userService.getPublicProfile(id, requesterId, isAdmin);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    /**
     * Update own profile (or any profile, if admin).
     * avatarUrl is ignored here — it can only change via /{id}/avatar.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long id,
            @RequestBody UpdateProfileRequest body,
            HttpServletRequest request
    ) {
        String requesterId = (String) request.getAttribute("userId");
        if (isAdminOrSelf(requesterId, id, request)) {
            return ResponseEntity.status(403).build();
        }
        try {
            User updated = userService.updateProfile(id, body);
            return ResponseEntity.ok(new UserDto(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Upload or replace the current user's avatar. Changed from PATCH to POST to match the frontend. */
    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(
            @PathVariable Long id,
            @RequestParam MultipartFile file,
            HttpServletRequest request
    ) {
        String requesterId = (String) request.getAttribute("userId");
        if (isAdminOrSelf(requesterId, id, request)) {
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

    /**
     * Marks the caller offline. Auth-derived, ignores the request body —
     * the frontend sends the whole user object, but we don't trust client input for this.
     */
    @PatchMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId != null) {
            userService.setConnected(Long.valueOf(userId), false);
        }
        return ResponseEntity.ok().build();
    }

    public record AvatarResponse(String avatarUrl) {}

    private boolean isAdminOrSelf(String requesterId, Long targetId, HttpServletRequest request) {
        if (requesterId == null) return true;
        if (Long.valueOf(requesterId).equals(targetId)) return false;
        return !request.isUserInRole("ROLE_ADMIN");
    }
}