package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.MessageDTO;
import com.evolt.chatapp.repositories.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserService userService;

    public MessageService(MessageRepository messageRepository, UserService userService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    public Message saveMessage(String senderUsername, String receiverUsername, String content, String timestamp) {

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

        return messageRepository.save(message);
    }

    public List<Message> findPrivateChat(User sender, User receiver) {
        return messageRepository.findBySenderAndReceiverOrReceiverAndSenderOrderByTimestampAsc(
                sender, receiver, sender, receiver
        );
    }

    public List<Message> findGlobalChat() {
        return messageRepository.findByReceiverOrderByTimestampAsc(null);
    }
}