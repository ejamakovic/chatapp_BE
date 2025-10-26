package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.MessageDTO;
import com.evolt.chatapp.services.MessageService;
import com.evolt.chatapp.services.UserService;
import com.evolt.chatapp.websocket.ChatWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public MessageController(MessageService messageService, UserService userService, ChatWebSocketHandler chatWebSocketHandler) {
        this.messageService = messageService;
        this.userService = userService;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @GetMapping
    public List<Message> getAllMessages() {
        return messageService.getAllMessages();
    }

    @PostMapping("/create")
    public ResponseEntity<Message> createMessage(@RequestBody MessageDTO dto) {
        try {
            User sender = userService.findByUsername(dto.getSender().getUsername());
            User receiver = dto.getReceiver() != null && dto.getReceiver().getUsername() != null
                    ? userService.findByUsername(dto.getReceiver().getUsername())
                    : null;

            Message msg = new Message();
            msg.setSender(sender);
            msg.setReceiver(receiver);
            msg.setContent(dto.getContent());

            messageService.saveMessage(msg);
            dto.setTimestamp(String.valueOf(msg.getTimestamp()));
            chatWebSocketHandler.notifyNewMessage(dto);
            return ResponseEntity.ok(msg);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
