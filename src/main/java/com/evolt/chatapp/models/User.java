package com.evolt.chatapp.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private Boolean connected = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "sender")
    private List<Message> messages;

    @OneToMany(mappedBy = "user")
    private List<ConversationParticipant> conversations;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public List<ConversationParticipant> getConversations() {
        return conversations;
    }

    public void setConversations(List<ConversationParticipant> conversations) {
        this.conversations = conversations;
    }
}


