package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.dto.CommentReactionDto;
import com.evolt.chatapp.services.CommentReactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts/comments/{commentId}/reactions")
public class CommentReactionController {

    private final CommentReactionService reactionService;

    public CommentReactionController(CommentReactionService reactionService) {
        this.reactionService = reactionService;
    }

    @GetMapping
    public ResponseEntity<List<CommentReactionDto>> getReactions(@PathVariable Long commentId) {
        return ResponseEntity.ok(reactionService.getReactions(commentId));
    }

    @PostMapping
    public ResponseEntity<?> setReaction(@PathVariable Long commentId, @RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = Long.parseLong(request.getAttribute("userId").toString());
        try {
            return ResponseEntity.ok(reactionService.setReaction(commentId, userId, body.get("emoji")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}