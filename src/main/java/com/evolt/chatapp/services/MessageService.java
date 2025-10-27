package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.repositories.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public List<Message> getAllMessages(){
        return messageRepository.findAll();
    }

    public Message saveMessage(Message mess){
        return messageRepository.save(mess);
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
