package com.evolt.chatapp.models.dto;

/**
 * Returned by /auth/login and /auth/register.
 * accessToken  – short-lived JWT (15 min), sent in Authorization header by client.
 * refreshToken – long-lived opaque token (7 days), stored HttpOnly cookie.
 */
public class AuthResponse {

    private String  accessToken;
    private String  refreshToken;
    private UserDto user;

    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, UserDto user) {
        this.accessToken  = accessToken;
        this.refreshToken = refreshToken;
        this.user         = user;
    }

    public String  getAccessToken()              { return accessToken; }
    public void    setAccessToken(String v)      { this.accessToken = v; }

    public String  getRefreshToken()             { return refreshToken; }
    public void    setRefreshToken(String v)     { this.refreshToken = v; }

    public UserDto getUser()                     { return user; }
    public void    setUser(UserDto v)            { this.user = v; }
}