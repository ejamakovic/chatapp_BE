package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.services.MessageService;
import com.evolt.chatapp.websocket.ChatWebSocketHandler;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    private final ChatWebSocketHandler chatWebSocketHandler;

    public MessageController(final MessageService messageService, ChatWebSocketHandler chatWebSocketHandler) {
        this.messageService = messageService;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @GetMapping
    public List<Message> getAllMessages() {
        return messageService.getAllMessages();
    }

    @PostMapping("/create")
    public void createMessage(@RequestBody final Message message) {
        Message savedMessage = messageService.saveMessage(message);
        System.out.println(savedMessage);
        chatWebSocketHandler.notifyNewMessage(savedMessage);
    }

}
