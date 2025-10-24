package com.evolt.chatapp.models.dto;

import com.evolt.chatapp.models.User;

public class UserDTO {
    private Long id;
    private String username;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
    }

    public UserDTO() {
    }

    public UserDTO(Long id) {
        this.id = id;
    }

    public UserDTO(String username) {
        this.username = username;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
