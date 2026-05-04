package com.evolt.chatapp.services;

import com.evolt.chatapp.models.User;
import com.evolt.chatapp.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public User createIfNotExists(String id, String username) {
        return userRepository.findById(Long.valueOf(id))
                .orElseGet(() -> {
                    User u = new User();
                    u.setId(Long.valueOf(id));
                    u.setUsername(username);
                    u.setConnected(true);
                    return userRepository.save(u);
                });
    }

    public User createIfNotExists(String username) {
        Optional<User> user = Optional.ofNullable(userRepository.findByUsername(username));
        if(user.isPresent()) {
            return user.orElse(null);
        }
        return saveUser(new User(username));
    }
}
