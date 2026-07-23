package com.evolt.chatapp.services;

import com.evolt.chatapp.models.dto.MessageWindowDto;
import com.evolt.chatapp.repositories.ConversationMemberRepository;
import com.evolt.chatapp.websocket.WebSocketEvent;
import com.evolt.chatapp.models.*;
import com.evolt.chatapp.models.dto.MessageDto;
import com.evolt.chatapp.repositories.ConversationRepository;
import com.evolt.chatapp.repositories.MessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserService userService;
    private final AttachmentService attachmentService;
    private final ApplicationEventPublisher eventPublisher;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    public MessageService(
            MessageRepository messageRepository,
            UserService userService,
            AttachmentService attachmentService,
            ApplicationEventPublisher eventPublisher,
            ConversationRepository conversationRepository,
            ConversationMemberRepository conversationMemberRepository
            ) {
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.attachmentService = attachmentService;
        this.eventPublisher = eventPublisher;
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    @Transactional
    public Message save(Message message) {
        return messageRepository.save(message);
    }

    @Transactional
    public MessageDto saveMessageDTO(Long senderId, Long conversationId, String content, Long replyToMessageId,
                                     String timestamp, List<MultipartFile> files) {

        Optional<Conversation> conversation =
                conversationRepository.findById(conversationId);

        if (!conversation.isPresent()) {
            throw new AccessDeniedException(
                    "Conversation with id " + conversationId + " doesn't exist!"
            );
        }

        boolean isMember = conversationMemberRepository
                .findConversationMembersByConversationId(conversationId)
                .stream()
                .anyMatch(username -> username.equals(userService.findById(senderId).getUsername()));
        // cleaner: add a dedicated existsByConversationIdAndUserId query instead — see note below
        if (!isMember) {
            throw new AccessDeniedException("You are not a member of this conversation");
        }

        User sender = userService.findById(senderId);

        Optional<Message> replyTo =
                messageRepository.findById(replyToMessageId);

        if (!replyTo.isPresent()){
            throw new AccessDeniedException(
                    "Message to reply with id " + replyToMessageId + " doesn't exist!"
            );
        }

        if (replyTo.get().getConversation() != conversation.get()) {
            throw new AccessDeniedException("Reply message doesn't belong to the same conversation as message that is sent");
        }

        Message message = new Message(
                sender,
                conversation.orElse(null),
                content,
                replyTo.get()
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

    public MessageWindowDto getMessagesAroundLastSeen(Long conversationId, Long lastSeenMessageId, int before, int after) {
        if (lastSeenMessageId == null || lastSeenMessageId <= 0) {
            Page<Message> latest = messageRepository.findMessagesFromConversation(conversationId, PageRequest.of(0, before));
            List<Message> ordered = new java.util.ArrayList<>(latest.getContent());
            java.util.Collections.reverse(ordered);
            List<MessageDto> dtos = ordered.stream().map(MessageDto::new).toList();
            return new MessageWindowDto(dtos, latest.getContent().size() >= before, false, null);
        }

        Page<Message> olderPage = messageRepository.findMessagesUpToId(conversationId, lastSeenMessageId, PageRequest.of(0, before));
        List<Message> older = new java.util.ArrayList<>(olderPage.getContent());
        java.util.Collections.reverse(older);

        Page<Message> newerPage = messageRepository.findMessagesAfterId(conversationId, lastSeenMessageId, PageRequest.of(0, after));

        List<Message> combined = new java.util.ArrayList<>(older);
        combined.addAll(newerPage.getContent());

        List<MessageDto> dtos = combined.stream().map(MessageDto::new).toList();

        boolean hasMoreOlder = olderPage.getContent().size() >= before;
        boolean hasMoreNewer = newerPage.getContent().size() >= after;

        return new MessageWindowDto(dtos, hasMoreOlder, hasMoreNewer, lastSeenMessageId);
    }

    public List<MessageDto> getMessagesBefore(Long conversationId, Long messageId, int size) {
        Page<Message> page = messageRepository.findMessagesBeforeId(conversationId, messageId, PageRequest.of(0, size));
        List<Message> ordered = new java.util.ArrayList<>(page.getContent());
        java.util.Collections.reverse(ordered);
        return ordered.stream().map(MessageDto::new).toList();
    }

    @Transactional
    public MessageDto editMessage(Long messageId, Long userId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        if (!message.getSender().getId().equals(userId)) {
            throw new AccessDeniedException("You can only edit your own messages");
        }
        if (message.isDeleted()) throw new IllegalArgumentException("Cannot edit a deleted message");
        if (newContent == null || newContent.isBlank()) throw new IllegalArgumentException("Content cannot be empty");
        message.setContent(newContent.trim());
        message.setEditedAt(LocalDateTime.now());
        MessageDto dto = new MessageDto(messageRepository.save(message));
        eventPublisher.publishEvent(new WebSocketEvent<>("MESSAGE_EDITED", dto));
        return dto;
    }

    @Transactional
    public MessageDto softDeleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        if (!message.getSender().getId().equals(userId)) {
            throw new AccessDeniedException("You can only delete your own messages");
        }
        message.setDeleted(true);
        message.setDeletedAt(LocalDateTime.now());
        message.setContent(null);
        MessageDto dto = new MessageDto(messageRepository.save(message));
        eventPublisher.publishEvent(new WebSocketEvent<>("MESSAGE_EDITED", dto));
        return dto;
    }
}