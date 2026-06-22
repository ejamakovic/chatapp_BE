package com.evolt.chatapp.controllers;

import com.evolt.chatapp.services.FriendshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateFriendshipStatus(
            @PathVariable Long id,
            @RequestParam String status)
    {
        friendshipService.updateFriendship(id, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping()
    public ResponseEntity<Void> friendshipUser(
            @RequestParam Long requesterId,
            @RequestParam Long addresseeId)
    {
        friendshipService.sendRequest(requesterId, addresseeId);
        return ResponseEntity.ok().build();
    }

}
