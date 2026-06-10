package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Conversation;
import com.evolt.chatapp.models.ConversationMember;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.ConversationListDto;
import com.evolt.chatapp.models.enums.ConversationRole;
import com.evolt.chatapp.models.enums.ConversationType;
import com.evolt.chatapp.repositories.ConversationMemberRepository;
import com.evolt.chatapp.repositories.ConversationRepository;
import com.evolt.chatapp.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserRepository userRepository;

    public ConversationService(ConversationRepository conversationRepository, ConversationMemberRepository conversationMemberRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.userRepository = userRepository;
    }

    public Conversation findConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId).orElse(null);
    }

    @Transactional
    public Conversation saveConversation(Conversation conversation) {
        return conversationRepository.save(conversation);
    }

    @Transactional
    public Conversation findOrCreateGlobalConversation() {
        Conversation conv = conversationRepository.findGlobalConversation();

        if (conv != null) return conv;

        Conversation newConv = new Conversation();
        newConv.setType(ConversationType.GLOBAL);

        return conversationRepository.save(newConv);
    }

    public Page<ConversationListDto> getUserConversations(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return conversationRepository.findUserConversations(userId, pageable);
    }

    @Transactional
    public Conversation findOrCreatePrivateConversation(Long senderId, Long receiverId) {
        Optional<Conversation> conversation = conversationRepository.findPrivateConversation(senderId, receiverId);

        if (conversation.isPresent()) {
            return conversation.get();
        }

        Conversation newConversation = new Conversation();
        newConversation.setType(ConversationType.PRIVATE);
        newConversation = conversationRepository.save(newConversation);

        User sender = userRepository.getReferenceById(senderId);
        User receiver = userRepository.getReferenceById(receiverId);

        ConversationMember conversationMember1 = new ConversationMember(newConversation, sender, ConversationRole.ADMIN);
        ConversationMember conversationMember2 = new ConversationMember(newConversation, receiver, ConversationRole.ADMIN);

        conversationMemberRepository.save(conversationMember1);
        conversationMemberRepository.save(conversationMember2);

        return newConversation;
    }

    @Transactional
    public Conversation addUserToConversation(Long conversationId, Long userId) {
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);
        if (conversation.isPresent()) {
            User user = userRepository.getReferenceById(userId);
            ConversationMember conversationMember = new ConversationMember(conversation.get(), user, ConversationRole.MEMBER);
            conversationMemberRepository.save(conversationMember);
            return conversation.get();
        }
        return null;
    }
}
