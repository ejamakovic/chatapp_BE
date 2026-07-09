package com.evolt.chatapp.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
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