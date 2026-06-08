package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Conversation;
import com.evolt.chatapp.models.ConversationMember;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.UserDTO;
import com.evolt.chatapp.models.enums.ConversationRole;
import com.evolt.chatapp.services.ConversationMemberService;
import com.evolt.chatapp.services.ConversationService;
import com.evolt.chatapp.services.UserService;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/connected")
    public List<User> allConnectedUsers() {
        return userService.getAllUsersByConnected();
    }

    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.createIfNotExists(userDTO.getUsername()));
    }

    @PutMapping("/logout")
    public ResponseEntity<Void> logoutUser(@RequestBody UserDTO userDTO) {

        User user = userService.findByUsername(userDTO.getUsername());

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        user.setConnected(false);
        userService.saveUser(user);

        return ResponseEntity.ok().build();
    }
}
