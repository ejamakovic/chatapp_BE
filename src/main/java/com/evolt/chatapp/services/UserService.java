package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Conversation;
import com.evolt.chatapp.models.ConversationMember;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.repositories.ConversationMemberRepository;
import com.evolt.chatapp.repositories.ConversationRepository;
import com.evolt.chatapp.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final ConversationRepository conversationRepository;

    public UserService(UserRepository userRepository, ConversationMemberRepository conversationMemberRepository, ConversationRepository conversationRepository) {
        this.userRepository = userRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.conversationRepository = conversationRepository;
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
        User existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            return existingUser;
        }

        User newUser = new User(username);
        newUser.setConnected(true);
        newUser = userRepository.save(newUser);

        Conversation globalChat = conversationRepository.findGlobalConversation();

        ConversationMember newMember = new ConversationMember();
        newMember.setUser(newUser);
        newMember.setConversation(globalChat);

        conversationMemberRepository.save(newMember);

        return newUser;
    }

}
