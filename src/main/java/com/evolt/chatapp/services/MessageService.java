package com.evolt.chatapp.services;

import com.evolt.chatapp.models.*;
import com.evolt.chatapp.models.dto.MessageDTO;
import com.evolt.chatapp.repositories.MessageRepository;
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
    private final ConversationService conversationService;

    public MessageService(
            MessageRepository messageRepository,
            UserService userService,
            AttachmentService attachmentService,
            ConversationService conversationService
    ) {
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.attachmentService = attachmentService;
        this.conversationService = conversationService;
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    public MessageDTO saveMessageDTO(
            Long senderId,
            Long conversationId,
            String content,
            String timestamp
    ) {

        User sender = userService.findById(senderId);

        Optional<Conversation> conversation = Optional.empty();

        if (conversationId != null) {
            conversation = conversationService.findConversationById(conversationId);
        }

        Message message = new Message(sender, conversation.orElse(null), content);

        return new MessageDTO(messageRepository.save(message));
    }

    public MessageDTO saveMessageDTOFile(
            Long senderId,
            Long conversationId,
            String content,
            String timestamp,
            MultipartFile file
    ) throws IOException {

        User sender = userService.findById(senderId);

        Optional<Conversation> conversation = conversationService.findConversationById(conversationId);

        Message message = new Message(sender, conversation.orElse(null), content);

        Message savedMessage = messageRepository.save(message);

        if (file != null && !file.isEmpty()) {
            attachmentService.saveAttachment(
                    file,
                    savedMessage
            );
        }

        return new MessageDTO(savedMessage);
    }

    public Page<Message> getChat(Long conversationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findPrivateChat(conversationId, pageable);
    }

    public Optional<Message> findMessageById(Long id) {
        return messageRepository.findById(id);
    }

    public void deleteById(Long id) {
        messageRepository.deleteById(id);
    }

    public Page<Message> getMessagesByConversation(Long id, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findMessagesFromConversation(id, pageable);
    }
}