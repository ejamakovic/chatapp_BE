package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.dto.MessageReactionDto;
import com.evolt.chatapp.models.enums.AllowedReactions;
import com.evolt.chatapp.services.MessageReactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages/{messageId}/reactions")
public class MessageReactionController {

    private final MessageReactionService reactionService;

    public MessageReactionController(MessageReactionService reactionService) {
        this.reactionService = reactionService;
    }

    @GetMapping
    public ResponseEntity<List<MessageReactionDto>> getReactions(@PathVariable Long messageId) {
        return ResponseEntity.ok(reactionService.getReactionsForMessage(messageId));
    }

    @PostMapping
    public ResponseEntity<?> addReaction(
            @PathVariable Long messageId,
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        Long userId = Long.parseLong(request.getAttribute("userId").toString());
        String emoji = body.get("emoji");

        try {
            MessageReactionDto dto = reactionService.addReaction(messageId, userId, emoji);
            return ResponseEntity.ok(dto); // dto may be null if already reacted — still 200, idempotent
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> removeReaction(
            @PathVariable Long messageId,
            @RequestParam String emoji,
            HttpServletRequest request
    ) {
        Long userId = Long.parseLong(request.getAttribute("userId").toString());
        reactionService.removeReaction(messageId, userId, emoji);
        return ResponseEntity.ok().build();
    }

    /** Lets the frontend fetch the allowed emoji set instead of hardcoding it twice. */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableEmojis() {
        return ResponseEntity.ok(AllowedReactions.EMOJIS);
    }
}