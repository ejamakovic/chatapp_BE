package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Conversation;
import com.evolt.chatapp.models.dto.ConversationListDto;
import com.evolt.chatapp.models.dto.GroupConversationRequest;
import com.evolt.chatapp.services.ConversationMemberService;
import com.evolt.chatapp.services.ConversationService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
            @RequestParam Long receiverId,
            HttpServletRequest request
    ) {
        Long senderId = Long.parseLong(request.getAttribute("userId").toString());
        return ResponseEntity.ok(conversationService.findOrCreatePrivateConversation(senderId, receiverId));
    }

    @PostMapping("/group")
    public ResponseEntity<?> createGroupConversation(@RequestBody GroupConversationRequest request) {
        try {
            Conversation conversation = conversationService.createGroupConversation(
                    request.getName(), request.getMemberIds());
            return ResponseEntity.ok(conversation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{conversationId}/last-seen")
    public ResponseEntity<Void> updateLastSeenMessage(
            @PathVariable Long conversationId,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request
    ) {
        Long userId = Long.parseLong(request.getAttribute("userId").toString());
        Long lastSeenMessageId = payload.get("lastSeenMessageId") != null
                ? Long.valueOf(payload.get("lastSeenMessageId").toString()) : null;

        if (lastSeenMessageId == null) {
            return ResponseEntity.badRequest().build();
        }

        conversationMemberService.updateLastSeenMessage(userId, conversationId, lastSeenMessageId);
        return ResponseEntity.ok().build();
    }
}