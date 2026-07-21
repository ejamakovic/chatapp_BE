package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.dto.PostReactionDto;
import com.evolt.chatapp.services.PostReactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts/{postId}/reactions")
public class PostReactionController {

    private final PostReactionService reactionService;

    public PostReactionController(PostReactionService reactionService) {
        this.reactionService = reactionService;
    }

    @GetMapping
    public ResponseEntity<List<PostReactionDto>> getReactions(@PathVariable Long postId) {
        return ResponseEntity.ok(reactionService.getReactions(postId));
    }

    @PostMapping
    public ResponseEntity<?> setReaction(@PathVariable Long postId, @RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = Long.parseLong(request.getAttribute("userId").toString());
        try {
            return ResponseEntity.ok(reactionService.setReaction(postId, userId, body.get("emoji")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}