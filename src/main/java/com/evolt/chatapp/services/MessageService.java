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
import java.time.LocalDateTime;
import java.util.List;

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

        Conversation conversation = conversationService.findConversationById(conversationId);

        Message message = new Message(sender, conversation, content);

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

        Conversation conversation = conversationService.findConversationById(conversationId);

        Message message = new Message(sender, conversation, content);

        Message savedMessage = messageRepository.save(message);

        if (file != null && !file.isEmpty()) {

            Attachment attachment =
                    attachmentService.saveAttachment(
                            file,
                            savedMessage
                    );
        }

        return new MessageDTO(savedMessage);
    }

    public List<Message> getPrivateChat(Long senderId, Long conversationId) {
        return messageRepository.
    }

    public List<Message> getGlobalChat() {
        return messageRepository.findByReceiverOrderByTimestampAsc(null);
    }

    public Page<Message> findGlobalChat(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByReceiverIsNull(pageable);
    }

    // Need to search by conversation not sender and receiver!!!
    public Page<Message> findPrivateChat(User sender, User receiver, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findPrivateChat(sender, receiver, pageable);
    }

    public Page<Message> getAllPrivateChatsForUser(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findAllPrivateChatsFromUser(user, pageable);
    }
}