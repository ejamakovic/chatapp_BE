package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Conversation;
import com.evolt.chatapp.models.enums.ConversationType;
import com.evolt.chatapp.repositories.ConversationRepository;
import org.springframework.stereotype.Service;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public Conversation findConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId).orElse(null);
    }

    public Conversation saveConversation(Conversation conversation) {
        return conversationRepository.save(conversation);
    }

    public Conversation findOrCreateGlobalConversation() {
        Conversation conv = conversationRepository.findGlobalConversation();

        if (conv != null) return conv;

        Conversation newConv = new Conversation();
        newConv.setType(ConversationType.GLOBAL);

        return conversationRepository.save(newConv);
    }
}
