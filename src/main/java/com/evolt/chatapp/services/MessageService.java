package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Attachment;
import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.User;
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

    public MessageService(MessageRepository messageRepository, UserService userService, AttachmentService attachmentService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.attachmentService = attachmentService;
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    public MessageDTO saveMessageDTO(String senderUsername, String receiverUsername, String content, String timestamp) {

        User sender = userService.findByUsername(senderUsername);

        User receiver = null;
        if (receiverUsername != null) {
            receiver = userService.findByUsername(receiverUsername);
        }

        Message message = new Message(sender, receiver, content);
        message.setTimestamp(
                timestamp != null
                        ? LocalDateTime.parse(timestamp)
                        : LocalDateTime.now()
        );

        return new MessageDTO(messageRepository.save(message));
    }

    public MessageDTO saveMessageDTOFile(
            String senderUsername,
            String receiverUsername,
            String content,
            String timestamp,
            MultipartFile file
    ) throws IOException {

        User sender = userService.findByUsername(senderUsername);

        User receiver = null;

        if (receiverUsername != null) {
            receiver = userService.findByUsername(receiverUsername);
        }

        Message message = new Message();

        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);

        message.setTimestamp(
                timestamp != null
                        ? LocalDateTime.parse(timestamp)
                        : LocalDateTime.now()
        );

        Message savedMessage = messageRepository.save(message);

        // attachment
        if (file != null && !file.isEmpty()) {

            Attachment attachment =
                    attachmentService.saveAttachment(
                            file,
                            savedMessage
                    );

            savedMessage
                    .getAttachments()
                    .add(attachment);

            savedMessage = messageRepository.save(savedMessage);
        }

        return new MessageDTO(savedMessage);
    }

    public List<Message> getPrivateChat(User sender, User receiver) {
        return messageRepository.findBySenderAndReceiverOrReceiverAndSenderOrderByTimestampAsc(
                sender, receiver, sender, receiver
        );
    }

    public List<Message> getGlobalChat() {
        return messageRepository.findByReceiverOrderByTimestampAsc(null);
    }

    public Page<Message> findGlobalChat(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByReceiverIsNull(pageable);
    }

    public Page<Message> findPrivateChat(User sender, User receiver, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findPrivateChat(sender, receiver, pageable);
    }

    public Page<Message> getAllPrivateChatsForUser(User sender, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findAllPrivateChatsFromUser(sender, pageable);
    }
}