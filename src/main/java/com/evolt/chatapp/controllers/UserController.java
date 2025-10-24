package com.evolt.chatapp.controllers;


import com.evolt.chatapp.models.User;
import com.evolt.chatapp.services.UserService;
import com.evolt.chatapp.websocket.ChatWebSocketHandler;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    private final ChatWebSocketHandler chatWebSocketHandler;

    public UserController(UserService userService, ChatWebSocketHandler chatWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.userService = userService;
    }

    @GetMapping
    public List<User> allUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/connected")
    public List<User> allConnectedUsers() {
        return userService.getAllUsersByConnected();
    }

    @PostMapping("/{username}")
    public void createUser(@PathVariable String username) {
        User user = new User(username);
        User savedUser = userService.saveUser(user);
        chatWebSocketHandler.notifyNewUser(savedUser);
    }

}
