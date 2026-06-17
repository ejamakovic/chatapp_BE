package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Conversation;
import com.evolt.chatapp.models.dto.ConversationListDto;
import com.evolt.chatapp.services.ConversationMemberService;
import com.evolt.chatapp.services.ConversationService;
import com.evolt.chatapp.websocket.ChatWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private static final Logger logger = LoggerFactory.getLogger(ConversationController.class);
    private final ConversationService conversationService;
    private final ConversationMemberService conversationMemberService;

    public ConversationController(ConversationService conversationService, ConversationMemberService conversationMemberService) {
        this.conversationService = conversationService;
        this.conversationMemberService = conversationMemberService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Conversation> getConversation(@PathVariable Long id) {
        return ResponseEntity.ok(conversationService.findConversationById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<Conversation> createConversation(@RequestBody Conversation conversation) {
        return ResponseEntity.ok(conversationService.saveConversation(conversation));
    }

    @GetMapping("/global")
    public ResponseEntity<Conversation> getConversationGlobal() {
        return ResponseEntity.ok(conversationService.findOrCreateGlobalConversation());
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<Page<ConversationListDto>> getUsersConversation(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ) {
        return ResponseEntity.ok(conversationService.getUserConversations(id, page, size));
    }

    @GetMapping("/private")
    public ResponseEntity<Conversation> getOrCreatePrivateConversation(
            @RequestParam Long senderId,
            @RequestParam Long receiverId
    ) {
        return ResponseEntity.ok(conversationService.findOrCreatePrivateConversation(senderId, receiverId));
    }

    @PatchMapping("/{conversationId}/last-seen")
    public ResponseEntity<Void> updateLastSeenMessage(
            @PathVariable Long conversationId,
            @RequestBody java.util.Map<String, Object> payload
    ) {
        // 🔍 LOG 5: Log incoming request parameters at controller boundary
        logger.info("📥 Received PATCH layout for Conversation ID: {} | Raw Payload: {}", conversationId, payload);

        // Explicitly parse values safely
        Long userId = payload.get("userId") != null ? Long.valueOf(payload.get("userId").toString()) : null;
        Long lastSeenMessageId = payload.get("lastSeenMessageId") != null ? Long.valueOf(payload.get("lastSeenMessageId").toString()) : null;

        logger.info("🔍 Extracted parameters -> userId: {}, lastSeenMessageId: {}", userId, lastSeenMessageId);

        if (userId == null || lastSeenMessageId == null) {
            logger.warn("⚠️ Aborting update: Extracted payload values contain null identifiers.");
            return ResponseEntity.badRequest().build();
        }

        conversationMemberService.updateLastSeenMessage(userId, conversationId, lastSeenMessageId);
        logger.info("✅ Dispatched update tracking values down to database layer service execution flow.");

        return ResponseEntity.ok().build();
    }
}
