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

    /** Same emoji again removes it; a different emoji overwrites it. Returns null when removed. */
    @PostMapping
    public ResponseEntity<?> setReaction(@PathVariable Long messageId, @RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = Long.parseLong(request.getAttribute("userId").toString());
        try {
            return ResponseEntity.ok(reactionService.setReaction(messageId, userId, body.get("emoji")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableEmojis() {
        return ResponseEntity.ok(AllowedReactions.EMOJIS);
    }
}