package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Conversation;
import com.evolt.chatapp.services.ConversationMemberService;
import com.evolt.chatapp.services.ConversationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

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
    public ResponseEntity<Page<Conversation>> getUsersConversation(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ) {
        return ResponseEntity.ok(conversationMemberService.getUserConversations(id, page, size));
    }

}
