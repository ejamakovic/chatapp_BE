package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Conversation;
import com.evolt.chatapp.models.ConversationMember;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.ConversationListDto;
import com.evolt.chatapp.models.dto.UserDto;
import com.evolt.chatapp.models.enums.ConversationRole;
import com.evolt.chatapp.models.enums.ConversationType;
import com.evolt.chatapp.repositories.ConversationMemberRepository;
import com.evolt.chatapp.repositories.ConversationRepository;
import com.evolt.chatapp.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * Creates a new GROUP conversation. The first member becomes OWNER,
     * everyone else joins as MEMBER. Requires at least 2 distinct members.
     */
    @Transactional
    public Conversation createGroupConversation(String name, List<Long> memberIds) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Group name is required");
        }
        if (memberIds == null) {
            throw new IllegalArgumentException("At least 2 members are required");
        }

        Set<Long> uniqueMemberIds = new LinkedHashSet<>(memberIds);
        if (uniqueMemberIds.size() < 2) {
            throw new IllegalArgumentException("At least 2 distinct members are required");
        }

        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.GROUP);
        conversation.setName(name.trim());
        conversation = conversationRepository.save(conversation);

        boolean first = true;
        for (Long memberId : uniqueMemberIds) {
            User user = userRepository.getReferenceById(memberId);
            ConversationRole role = first ? ConversationRole.OWNER : ConversationRole.MEMBER;
            conversationMemberRepository.save(new ConversationMember(conversation, user, role));
            first = false;
        }

        return conversation;
    }

    @Transactional
    public Conversation updateGroupDetails(Long conversationId, Long requesterId, String name, String imageUrl) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (conversation.getType() != ConversationType.GROUP) {
            throw new IllegalArgumentException("Only group conversations can be edited");
        }

        ConversationMember requester = conversationMemberRepository
                .findByConversationIdAndUserId(conversationId, requesterId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member"));
        if (requester.getRole() == ConversationRole.MEMBER) {
            throw new AccessDeniedException("Only admins/owner can edit group details");
        }

        if (name != null && !name.isBlank()) conversation.setName(name.trim());
        if (imageUrl != null) conversation.setImageUrl(imageUrl);

        return conversationRepository.save(conversation);
    }

    @Transactional
    public String updateGroupImage(Long conversationId, Long requesterId, MultipartFile file) throws IOException {

        ConversationMember requester = conversationMemberRepository
                .findByConversationIdAndUserId(conversationId, requesterId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member"));
        if (requester.getRole() == ConversationRole.MEMBER) {
            throw new AccessDeniedException("Only admins/owner can edit group details");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Group image must be an image file");
        }

        Path uploadDir = Paths.get("uploads", "groups");
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), uploadDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        String url = "/uploads/groups/" + fileName;

        Conversation conversation = updateGroupDetails(conversationId, requesterId, null, url);
        return url;
    }

    public List<UserDto> getUsersNotInConversation(Long conversationId) {
        Set<Long> memberIds = conversationMemberRepository.findAllWithUserByConversationId(conversationId)
                .stream().map(cm -> cm.getUser().getId()).collect(Collectors.toSet());
        return userRepository.findAll().stream()
                .filter(u -> !memberIds.contains(u.getId()))
                .map(UserDto::new)
                .toList();
    }
}