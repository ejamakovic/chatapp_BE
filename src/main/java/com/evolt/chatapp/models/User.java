package com.evolt.chatapp.models;

import com.evolt.chatapp.models.enums.UserRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "index_user_id",       columnList = "id"),
                @Index(name = "index_user_username",  columnList = "username"),
                @Index(name = "index_user_email",     columnList = "email")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * BCrypt-hashed password — never store plain text.
     */
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    /**
     * Relative URL served by the static-resource handler, e.g. /uploads/avatars/uuid.jpg
     */
    @Column(length = 512)
    private String avatarUrl;

    @Column(nullable = false)
    private boolean connected = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email    = email;
        this.password = password;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public String getUsername()                { return username; }
    public void setUsername(String username)   { this.username = username; }

    public String getPassword()                { return password; }
    public void setPassword(String password)   { this.password = password; }

    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }

    public String getFirstName()               { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName()                { return lastName; }
    public void setLastName(String lastName)   { this.lastName = lastName; }

    public String getAvatarUrl()               { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public boolean isConnected()               { return connected; }
    public void setConnected(boolean connected){ this.connected = connected; }

    public UserRole getRole()                  { return role; }
    public void setRole(UserRole role)         { this.role = role; }

    public LocalDateTime getCreatedAt()        { return createdAt; }
    public void setCreatedAt(LocalDateTime v)  { this.createdAt = v; }
}