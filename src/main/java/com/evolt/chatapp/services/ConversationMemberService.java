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
    public ConversationMember save(ConversationMember conversationMember) {
        return conversationMemberRepository.save(conversationMember);
    }

    public ConversationMember getById(Long id) {
        return conversationMemberRepository.findById(id).orElse(null);
    }

    public List<ConversationMember> getAll() {
        return conversationMemberRepository.findAll();
    }

    public List<String> getParticipants(Long conversationId) {
        return conversationMemberRepository.findConversationMembersByConversationId(conversationId);
    }

    @Transactional
    public void deleteById(Long id) {
        conversationMemberRepository.deleteById(id);
    }

    @Transactional
    public void updateLastSeenMessage(Long userId, Long conversationId, Long lastSeenMessageId) {
        conversationMemberRepository.updateLastSeenMessage(userId, conversationId, lastSeenMessageId);
    }

    public Long getLastSeenMessageId(Long userId, Long conversationId) {
        return conversationMemberRepository.findLastSeenMessageId(userId, conversationId);
    }
}
