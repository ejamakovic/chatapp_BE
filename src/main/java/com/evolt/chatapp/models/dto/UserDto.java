package com.evolt.chatapp.models.dto;

import com.evolt.chatapp.models.User;

public class UserDto {
    private Long id;
    private String username;

    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
    }

    public UserDto() {
    }

    public UserDto(Long id, String username) {
        this.id = id;
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
