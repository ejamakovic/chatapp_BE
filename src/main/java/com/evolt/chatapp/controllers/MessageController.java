package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.MessageDTO;
import com.evolt.chatapp.services.MessageService;
import com.evolt.chatapp.services.UserService;
import com.evolt.chatapp.websocket.ChatWebSocketHandler;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    private final ChatWebSocketHandler chatWebSocketHandler;

    public MessageController(final MessageService messageService, UserService userService, ChatWebSocketHandler chatWebSocketHandler) {
        this.messageService = messageService;
        this.userService = userService;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @GetMapping
    public List<Message> getAllMessages() {
        return messageService.getAllMessages();
    }

    @PostMapping("/create")
    public void createMessage(@RequestBody MessageDTO messageDTO) {
        User sender = userService.findById(messageDTO.getSender().getId());
        User receiver = null;
        if (messageDTO.getReceiver() != null) {
            receiver = userService.findById(messageDTO.getReceiver().getId());
        }

        Message message = new Message(sender, receiver, messageDTO.getContent());
        messageService.saveMessage(message);

        messageDTO.setTimestamp(message.getTimestamp().toString());
        chatWebSocketHandler.notifyNewMessage(messageDTO);
    }



}
