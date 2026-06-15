package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Conversation;
import com.evolt.chatapp.models.ConversationMember;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.UserDto;
import com.evolt.chatapp.models.enums.ConversationRole;
import com.evolt.chatapp.repositories.ConversationMemberRepository;
import com.evolt.chatapp.repositories.ConversationRepository;
import com.evolt.chatapp.repositories.NotificationRepository;
import com.evolt.chatapp.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final NotificationService notificationService;

    public UserService(UserRepository userRepository, ConversationRepository conversationRepository, ConversationMemberRepository conversationMemberRepository, NotificationRepository notificationRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.notificationService = notificationService;
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAllUsersByConnected() {
        return userRepository.findAllByConnected(true);
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User createIfNotExists(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return user;
        }
        user = saveUser(new User(username));
        Conversation conversation = conversationRepository.findGlobalConversation();
        ConversationMember conversationMember = new ConversationMember(conversation , user, ConversationRole.MEMBER);
        conversationMemberRepository.save(conversationMember);
        notificationService.createNewUserNotifications(new UserDto(user));
        return user;
    }

    @Transactional
    public void setConnected(String username, boolean b) {
        userRepository.setConnected(b, username);
    }


}
