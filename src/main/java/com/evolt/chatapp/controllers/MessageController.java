package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.dto.MessageDTO;
import com.evolt.chatapp.models.mappers.MessageMapper;
import com.evolt.chatapp.services.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public List<Message> getAllMessages() {
        return messageService.getAllMessages();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Message>> getMessageById(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.findMessageById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessageById(@PathVariable Long id) {
        try{
            messageService.deleteById(id);
        }
        catch (Exception e){
            logger.error("Error deleting message with id {}", id , e);
            ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/conversation/{id}")
    public ResponseEntity<?> getConversationById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        var messagesPage = messageService.getMessagesByConversation(id, page, size);

        var dtoPage = messagesPage.map(MessageMapper::toDTO);

        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping("/send")
    public ResponseEntity<MessageDTO> sendMessage(
            @RequestParam Long senderId,
            @RequestParam Long conversationId,
            @RequestParam String content,
            @RequestParam(required = false) List<MultipartFile> files
    ) {

        MessageDTO dto = messageService.saveMessageDTO(
                senderId,
                conversationId,
                content,
                null,
                files
        );

        return ResponseEntity.ok(dto);
    }

}
