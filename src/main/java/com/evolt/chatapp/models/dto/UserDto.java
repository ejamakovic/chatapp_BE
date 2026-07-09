package com.evolt.chatapp.models.dto;

import com.evolt.chatapp.models.User;

public class UserDto {

    private Long   id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private String bio;
    private String role;

    public UserDto() {}

    public UserDto(User user) {
        this.id        = user.getId();
        this.username  = user.getUsername();
        this.email     = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName  = user.getLastName();
        this.avatarUrl = user.getAvatarUrl();
        this.bio       = user.getBio();
        this.role      = user.getRole().name();
    }

    public UserDto(Long id, String username) {
        this.id       = id;
        this.username = username;
    }

    public Long   getId()                      { return id; }
    public void   setId(Long id)               { this.id = id; }

    public String getUsername()                { return username; }
    public void   setUsername(String username) { this.username = username; }

    public String getEmail()                   { return email; }
    public void   setEmail(String email)       { this.email = email; }

    public String getFirstName()               { return firstName; }
    public void   setFirstName(String v)       { this.firstName = v; }

    public String getLastName()                { return lastName; }
    public void   setLastName(String v)        { this.lastName = v; }

    public String getAvatarUrl()               { return avatarUrl; }
    public void   setAvatarUrl(String v)       { this.avatarUrl = v; }

    public String getBio()                     { return bio; }
    public void   setBio(String bio)           { this.bio = bio; }

    public String getRole()                    { return role; }
    public void   setRole(String role)         { this.role = role; }
}