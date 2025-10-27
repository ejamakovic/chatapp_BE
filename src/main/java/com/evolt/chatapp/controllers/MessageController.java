package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.MessageDTO;
import com.evolt.chatapp.models.dto.UserDTO;
import com.evolt.chatapp.services.MessageService;
import com.evolt.chatapp.services.UserService;
import com.evolt.chatapp.websocket.ChatWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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


    @GetMapping("/global")
    public List<Message> getGlobalChat() {
        return messageService.findGlobalChat();
    }

    @GetMapping("/private")
    public List<Message> getPrivateChat(@RequestParam String sender, @RequestParam String receiver) {
        User senderUser = userService.findByUsername(sender);
        User receiverUser = userService.findByUsername(receiver);
        return messageService.findPrivateChat(senderUser, receiverUser);
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

    @PostMapping("/private/start")
    public List<Message> createPrivateChatRequest(@RequestBody Map<String, String> payload) {
        String sender = payload.get("sender");
        String receiver = payload.get("receiver");

        User senderUser = userService.findByUsername(sender);
        User receiverUser = userService.findByUsername(receiver);

        chatWebSocketHandler.notifyNewChatRequest(sender, receiver);
        return messageService.findPrivateChat(senderUser, receiverUser);
    }

}
