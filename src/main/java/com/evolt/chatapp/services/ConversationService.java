package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Conversation;
import com.evolt.chatapp.repositories.ConversationRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public Optional<Conversation> findConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId);
    }

    public Conversation saveConversation(Conversation conversation) {
        return conversationRepository.save(conversation);
    }

    public Conversation findGlobalConversation() {
        return conversationRepository.findGlobalConversation();
    }
}
