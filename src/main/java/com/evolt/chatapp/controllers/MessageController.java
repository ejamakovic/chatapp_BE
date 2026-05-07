package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.MessageDTO;
import com.evolt.chatapp.models.dto.UserDTO;
import com.evolt.chatapp.models.mappers.MessageMapper;
import com.evolt.chatapp.services.MessageService;
import com.evolt.chatapp.services.UserService;
import com.evolt.chatapp.websocket.ChatWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

        var dtoPage = messagesPage.map(MessageMapper::toDTO);

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

        var dtoPage = messagesPage.map(MessageMapper::toDTO);

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

            MessageDTO responseDTO = MessageMapper.toDTO(msg);

            chatWebSocketHandler.notifyNewMessage(responseDTO);

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/allPrivateChats")
    public ResponseEntity<?> allPrivateChats(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size)
    {
            User user = userService.findByUsername(username);
            var chats = messageService.getAllPrivateChatsForUser(user, page, size);
            return ResponseEntity.ok(chats);
    }

    @PostMapping("/send")
    public ResponseEntity<MessageDTO> sendMessage(
            @RequestParam String sender,
            @RequestParam(required = false) String receiver,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) MultipartFile file
    ) throws IOException {

        MessageDTO saved;

        if (file != null && !file.isEmpty()) {

            saved = messageService.saveMessageDTOFile(
                    sender,
                    receiver,
                    content,
                    null,
                    file
            );

        } else {

            saved = messageService.saveMessageDTO(
                    sender,
                    receiver,
                    content,
                    null
            );
        }

        chatWebSocketHandler.notifyNewMessage(saved);

        return ResponseEntity.ok(saved);
    }

}
