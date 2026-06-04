package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Conversation;
import com.evolt.chatapp.models.dto.ConversationListDto;
import com.evolt.chatapp.services.ConversationMemberService;
import com.evolt.chatapp.services.ConversationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
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
}
