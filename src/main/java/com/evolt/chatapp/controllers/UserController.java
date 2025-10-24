package com.evolt.chatapp.controllers;


import com.evolt.chatapp.models.User;
import com.evolt.chatapp.services.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> allUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{b}")
    public List<User> allConnectedUsers(@PathVariable boolean b) {
        return userService.getAllUsersByConnected(b);
    }

    @PostMapping("/{username}")
    public void createUser(@PathVariable String username) {
        User user = new User(username);
        userService.saveUser(user);
    }
}
