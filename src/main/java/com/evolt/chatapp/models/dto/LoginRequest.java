package com.evolt.chatapp.models.dto;

/**
 * Request body for POST /auth/login
 */
public class LoginRequest {

    private String username;
    private String password;

    public LoginRequest() {}

    public String getUsername()          { return username; }
    public void   setUsername(String v)  { this.username = v; }

    public String getPassword()          { return password; }
    public void   setPassword(String v)  { this.password = v; }
}