package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.dto.AttachmentDto;
import com.evolt.chatapp.models.dto.MessageDto;
import com.evolt.chatapp.models.dto.MessageWindowDto;
import com.evolt.chatapp.models.mappers.MessageMapper;
import com.evolt.chatapp.repositories.AttachmentRepository;
import com.evolt.chatapp.services.AttachmentService;
import com.evolt.chatapp.services.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    private final AttachmentRepository attachmentRepository;
    private final AttachmentService attachmentService;

    public MessageController(MessageService messageService, AttachmentRepository attachmentRepository, AttachmentService attachmentService) {
        this.messageService = messageService;
        this.attachmentRepository = attachmentRepository;
        this.attachmentService = attachmentService;
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
            return ResponseEntity.notFound().build();
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendMessage(
            @RequestParam Long conversationId,
            @RequestParam String content,
            @RequestParam(required = false) List<MultipartFile> files,
            HttpServletRequest request
    ) {
        Long senderId = Long.parseLong(request.getAttribute("userId").toString());
        try {
            MessageDto dto = messageService.saveMessageDTO(senderId, conversationId, content, null, files);
            return ResponseEntity.ok(dto);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/conversation/{id}/window")
    public ResponseEntity<MessageWindowDto> getMessageWindow(
            @PathVariable Long id,
            @RequestParam(required = false) Long lastSeenMessageId,
            @RequestParam(defaultValue = "30") int before,
            @RequestParam(defaultValue = "30") int after
    ) {
        return ResponseEntity.ok(messageService.getMessagesAroundLastSeen(id, lastSeenMessageId, before, after));
    }

    @GetMapping("/conversation/{id}/before/{messageId}")
    public ResponseEntity<List<MessageDto>> getMessagesBefore(
            @PathVariable Long id,
            @PathVariable Long messageId,
            @RequestParam(defaultValue = "30") int size
    ) {
        return ResponseEntity.ok(messageService.getMessagesBefore(id, messageId, size));
    }

    @GetMapping("/conversation/{id}/media")
    public ResponseEntity<List<AttachmentDto>> getConversationMedia(@PathVariable Long id, HttpServletRequest request) {
        // reuse membership check pattern from your other endpoints
        List<AttachmentDto> media = attachmentService.findByConversationId(id)
                .stream().map(AttachmentDto::new).toList();
        return ResponseEntity.ok(media);
    }
}
