package com.evolt.chatapp.models;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "message_reactions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"message_id", "user_id", "emoji"})
        },
        indexes = {
                @Index(name = "idx_reaction_message", columnList = "message_id")
        }
)
public class MessageReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // This tells Hibernate to explicitly use NVARCHAR and handle Unicode emojis
    @Nationalized
    @Column(name = "emoji", length = 255)
    private String emoji;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public MessageReaction() {}

    public MessageReaction(Message message, User user, String emoji) {
        this.message = message;
        this.user = user;
        this.emoji = emoji;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}