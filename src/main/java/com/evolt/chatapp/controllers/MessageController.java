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

    private MessageDTO toDTO(Message msg) {
        MessageDTO dto = new MessageDTO();

        UserDTO sender = new UserDTO();
        sender.setUsername(msg.getSender().getUsername());

        dto.setSender(sender);

        if (msg.getReceiver() != null) {
            UserDTO receiver = new UserDTO();
            receiver.setUsername(msg.getReceiver().getUsername());
            dto.setReceiver(receiver);
        }

        dto.setContent(msg.getContent());
        dto.setTimestamp(String.valueOf(msg.getTimestamp()));

        return dto;
    }

    @GetMapping
    public List<Message> getAllMessages() {
        return messageService.getAllMessages();
    }

    @GetMapping("/global")
    public List<Message> getGlobalChat() {
        return messageService.getGlobalChat();
    }

    @GetMapping("/globalPage")
    public ResponseEntity<?> getGlobalChat(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        var messagesPage = messageService.findGlobalChat(page, size);

        var dtoPage = messagesPage.map(this::toDTO);

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/private")
    public List<Message> getPrivateChat(@RequestParam String sender, @RequestParam String receiver) {
        User senderUser = userService.findByUsername(sender);
        User receiverUser = userService.findByUsername(receiver);
        return messageService.getPrivateChat(senderUser, receiverUser);
    }

    @GetMapping("/privatePage")
    public ResponseEntity<?> getPrivateChat(
            @RequestParam String sender,
            @RequestParam String receiver,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        User senderUser = userService.findByUsername(sender);
        User receiverUser = userService.findByUsername(receiver);

        var messagesPage = messageService.findPrivateChat(senderUser, receiverUser, page, size);

        var dtoPage = messagesPage.map(this::toDTO);

        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping("/create")
    public ResponseEntity<MessageDTO> createMessage(@RequestBody MessageDTO dto) {
        try {
            User sender = userService.findByUsername(dto.getSender().getUsername());

            User receiver = dto.getReceiver() != null
                    ? userService.findByUsername(dto.getReceiver().getUsername())
                    : null;

            Message msg = new Message();
            msg.setSender(sender);
            msg.setReceiver(receiver);
            msg.setContent(dto.getContent());

            messageService.saveMessage(msg);

            MessageDTO responseDTO = toDTO(msg);

            chatWebSocketHandler.notifyNewMessage(responseDTO);

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}
