package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Conversation;
import com.evolt.chatapp.models.ConversationMember;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.RegisterRequest;
import com.evolt.chatapp.models.dto.UpdateProfileRequest;
import com.evolt.chatapp.models.dto.UserDto;
import com.evolt.chatapp.models.enums.ConversationRole;
import com.evolt.chatapp.repositories.ConversationMemberRepository;
import com.evolt.chatapp.repositories.ConversationRepository;
import com.evolt.chatapp.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository               userRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final ConversationRepository       conversationRepository;
    private final PasswordEncoder              passwordEncoder;

    public UserService(
            UserRepository               userRepository,
            ConversationMemberRepository conversationMemberRepository,
            ConversationRepository       conversationRepository,
            PasswordEncoder              passwordEncoder
    ) {
        this.userRepository               = userRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.conversationRepository       = conversationRepository;
        this.passwordEncoder              = passwordEncoder;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAllConnectedUsers() {
        return userRepository.findAllByConnected(true);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Returns a UserDto for public viewing (someone else's profile page).
     * Email is stripped unless the requester is viewing their own profile or is an admin.
     */
    public UserDto getPublicProfile(Long targetId, Long requesterId, boolean requesterIsAdmin) {
        User user = userRepository.findById(targetId).orElse(null);
        if (user == null) return null;

        UserDto dto = new UserDto(user);
        if (!targetId.equals(requesterId) && !requesterIsAdmin) {
            dto.setEmail(null);
        }
        return dto;
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()) != null) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setConnected(false);

        User saved = userRepository.save(user);

        Conversation globalChat = conversationRepository.findGlobalConversation();
        if (globalChat != null) {
            ConversationMember member = new ConversationMember(
                    globalChat, saved, ConversationRole.MEMBER);
            conversationMemberRepository.save(member);
        }

        return saved;
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    @Transactional
    public String updateAvatar(Long userId, MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Avatar must be an image file");
        }

        Path uploadDir = Paths.get("uploads", "avatars");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path   dest     = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        String publicUrl = "/uploads/avatars/" + fileName;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setAvatarUrl(publicUrl);
        userRepository.save(user);

        return publicUrl;
    }

    /**
     * Updates editable profile fields. Email uniqueness is re-checked when changed.
     * avatarUrl is deliberately NOT settable here — only via updateAvatar().
     */
    @Transactional
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            User existing = userRepository.findByEmail(request.getEmail());
            if (existing != null && !existing.getId().equals(userId)) {
                throw new IllegalArgumentException("Email already registered");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void setConnected(Long userId, boolean connected) {
        userRepository.findById(userId).ifPresent(u -> {
            u.setConnected(connected);
            userRepository.save(u);
        });
    }
}