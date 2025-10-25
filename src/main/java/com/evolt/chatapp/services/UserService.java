package com.evolt.chatapp.services;

import com.evolt.chatapp.models.User;
import com.evolt.chatapp.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAllUsersByConnected() {
        return userRepository.findAllByConnected(true);
    }

    public User saveUser(User user) {
        userRepository.save(user);
        return user;
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
