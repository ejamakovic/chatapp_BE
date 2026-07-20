package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.dto.UserDto;
import com.evolt.chatapp.services.FriendshipService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateFriendshipStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpServletRequest request)
    {
        Long userId = Long.parseLong(request.getAttribute("userId").toString());
        try {
            friendshipService.updateFriendship(id, status, userId);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping()
    public ResponseEntity<Void> friendshipUser(
            @RequestParam Long addresseeId,
            HttpServletRequest request)
    {
        Long requesterId = Long.parseLong(request.getAttribute("userId").toString());
        friendshipService.sendRequest(requesterId, addresseeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<Page<UserDto>> getFriends(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        return ResponseEntity.ok(friendshipService.getFriendsFromUser(id, page, size));
    }

}
