package com.evolt.chatapp.services;

import com.evolt.chatapp.models.ConversationMember;
import com.evolt.chatapp.repositories.ConversationMemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationMemberService {

    private final ConversationMemberRepository conversationMemberRepository;

    public ConversationMemberService(ConversationMemberRepository conversationMemberRepository) {
        this.conversationMemberRepository = conversationMemberRepository;
    }

    @Transactional
    public ConversationMember saveConversationMember(ConversationMember conversationMember) {
        return conversationMemberRepository.save(conversationMember);
    }

    public ConversationMember getConversationMemberById(Long id) {
        return conversationMemberRepository.findById(id).orElse(null);
    }

    public List<ConversationMember> getAllConversationMembers() {
        return conversationMemberRepository.findAll();
    }

    public List<String> getParticipants(Long conversationId) {
        return conversationMemberRepository.findConversationMembersByConversationId(conversationId);
    }
}
