package com.evolt.chatapp.services;

import com.evolt.chatapp.events.WebSocketEvent;
import com.evolt.chatapp.models.*;
import com.evolt.chatapp.models.dto.MessageDto;
import com.evolt.chatapp.repositories.ConversationRepository;
import com.evolt.chatapp.repositories.MessageRepository;
import com.evolt.chatapp.websocket.ChatWebSocketHandler;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserService userService;
    private final AttachmentService attachmentService;
    private final ApplicationEventPublisher eventPublisher;
    private final ConversationRepository conversationRepository;

    public MessageService(
            MessageRepository messageRepository,
            UserService userService,
            AttachmentService attachmentService,
            ApplicationEventPublisher eventPublisher,
            ConversationRepository conversationRepository) {
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.attachmentService = attachmentService;
        this.eventPublisher = eventPublisher;
        this.conversationRepository = conversationRepository;
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    @Transactional
    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    @Transactional
    public MessageDto saveMessageDTO(
            Long senderId,
            Long conversationId,
            String content,
            String timestamp,
            List<MultipartFile> files
    ) {

        User sender = userService.findById(senderId);

        Optional<Conversation> conversation =
                conversationRepository.findById(conversationId);

        Message message = new Message(
                sender,
                conversation.orElse(null),
                content
        );

        Message savedMessage = messageRepository.save(message);

        conversationRepository.updateLastMessage(conversation.get().getId(), message);

        // Save all files (images/videos)
        if (files != null && !files.isEmpty()) {

            for (MultipartFile file : files) {

                if (file != null && !file.isEmpty()) {

                    try {
                        Attachment attachment = attachmentService.saveAttachment(
                                file,
                                savedMessage
                        );

                        savedMessage.getAttachments().add(attachment);

                    } catch (IOException e) {
                        throw new RuntimeException(
                                "Failed to save attachment: " + file.getOriginalFilename(),
                                e
                        );
                    }
                }
            }
        }

        MessageDto messageDTO = new MessageDto(savedMessage);

        // --- PUBLISH EVENT INSTEAD OF DIRECT CALL ---
        eventPublisher.publishEvent(new WebSocketEvent<>("NEW_MESSAGE", messageDTO));
        return messageDTO;
    }


    public Optional<Message> findMessageById(Long id) {
        return messageRepository.findById(id);
    }

    @Transactional
    public void deleteById(Long id) {
        messageRepository.deleteById(id);
    }

    public Page<Message> getMessagesByConversation(Long conversationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findMessagesFromConversation(conversationId, pageable);
    }


}