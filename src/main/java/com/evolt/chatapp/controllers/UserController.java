package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.UserDTO;
import com.evolt.chatapp.services.UserService;
import com.evolt.chatapp.websocket.ChatWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public UserController(UserService userService, ChatWebSocketHandler chatWebSocketHandler) {
        this.userService = userService;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @GetMapping
    public List<User> allUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/connected")
    public List<User> allConnectedUsers() {
        return userService.getAllUsersByConnected();
    }

    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody UserDTO userDTO) {
        User user = new User(userDTO.getUsername());
        userService.saveUser(user);
        try {
            chatWebSocketHandler.notifyNewUser(userDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<User> logoutUser(@RequestBody UserDTO userDTO) {
        User user = userService.findByUsername(userDTO.getUsername());
        if (user != null) {
            user.setConnected(false);
            userService.saveUser(user);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }
}
