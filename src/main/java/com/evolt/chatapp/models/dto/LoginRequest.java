package com.evolt.chatapp.models.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {}

    public String getUsername()          { return username; }
    public void   setUsername(String v)  { this.username = v; }

    public String getPassword()          { return password; }
    public void   setPassword(String v)  { this.password = v; }
}