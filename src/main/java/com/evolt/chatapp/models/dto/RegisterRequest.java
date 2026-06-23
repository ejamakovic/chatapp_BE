package com.evolt.chatapp.models.dto;

/**
 * Request body for POST /auth/register
 */
public class RegisterRequest {

    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;

    public RegisterRequest() {}

    public String getUsername()              { return username; }
    public void   setUsername(String v)      { this.username = v; }

    public String getEmail()                 { return email; }
    public void   setEmail(String v)         { this.email = v; }

    public String getPassword()              { return password; }
    public void   setPassword(String v)      { this.password = v; }

    public String getFirstName()             { return firstName; }
    public void   setFirstName(String v)     { this.firstName = v; }

    public String getLastName()              { return lastName; }
    public void   setLastName(String v)      { this.lastName = v; }
}