package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Conversation;
import com.evolt.chatapp.models.ConversationMember;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.ConversationMemberDto;
import com.evolt.chatapp.models.enums.ConversationRole;
import com.evolt.chatapp.models.enums.ConversationType;
import com.evolt.chatapp.repositories.ConversationMemberRepository;
import com.evolt.chatapp.repositories.ConversationRepository;
import com.evolt.chatapp.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationMemberService {

    private final ConversationMemberRepository conversationMemberRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public ConversationMemberService(ConversationMemberRepository conversationMemberRepository, ConversationRepository conversationRepository, UserRepository userRepository) {
        this.conversationMemberRepository = conversationMemberRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
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

    @Transactional
    public ConversationMemberDto addMember(Long conversationId, Long requesterId, Long newUserId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (conversation.getType() != ConversationType.GROUP) {
            throw new IllegalArgumentException("Can only add members to group conversations");
        }
        requireMember(conversationId, requesterId);

        if (conversationMemberRepository.findByConversationIdAndUserId(conversationId, newUserId).isPresent()) {
            throw new IllegalArgumentException("User is already a member");
        }

        User user = userRepository.getReferenceById(newUserId);
        ConversationMember member = new ConversationMember(conversation, user, ConversationRole.MEMBER);
        return new ConversationMemberDto(conversationMemberRepository.save(member));
    }

    @Transactional
    public void removeMember(Long conversationId, Long requesterId, Long targetUserId) {
        ConversationMember requester = requireAdminOrOwner(conversationId, requesterId);
        ConversationMember target = conversationMemberRepository
                .findByConversationIdAndUserId(conversationId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (target.getRole() == ConversationRole.OWNER) {
            throw new AccessDeniedException("The owner cannot be removed");
        }
        conversationMemberRepository.delete(target);
    }

    @Transactional
    public ConversationMemberDto changeRole(Long conversationId, Long requesterId, Long targetUserId, String newRoleRaw) {
        ConversationMember requester = requireAdminOrOwner(conversationId, requesterId);
        if (requester.getRole() != ConversationRole.OWNER) {
            throw new AccessDeniedException("Only the owner can change roles");
        }

        ConversationRole newRole = ConversationRole.valueOf(newRoleRaw);
        if (newRole == ConversationRole.OWNER) {
            throw new IllegalArgumentException("Ownership transfer isn't supported here");
        }

        ConversationMember target = conversationMemberRepository
                .findByConversationIdAndUserId(conversationId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (target.getRole() == ConversationRole.OWNER) {
            throw new AccessDeniedException("Cannot change the owner's role");
        }

        target.setRole(newRole);
        return new ConversationMemberDto(conversationMemberRepository.save(target));
    }

    public List<ConversationMemberDto> getMembers(Long conversationId, Long requesterId) {
        requireMember(conversationId, requesterId);
        return conversationMemberRepository.findAllWithUserByConversationId(conversationId)
                .stream().map(ConversationMemberDto::new).toList();
    }

    private ConversationMember requireMember(Long conversationId, Long userId) {
        return conversationMemberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this conversation"));
    }

    private ConversationMember requireAdminOrOwner(Long conversationId, Long userId) {
        ConversationMember member = requireMember(conversationId, userId);
        if (member.getRole() != ConversationRole.ADMIN && member.getRole() != ConversationRole.OWNER) {
            throw new AccessDeniedException("Only admins/owner can do this");
        }
        return member;
    }
}
