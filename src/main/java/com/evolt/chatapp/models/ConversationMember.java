package com.evolt.chatapp.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "conversation_members",

        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"conversation_id", "user_id"}
                )
        },
        indexes = {

        }
    )
public class ConversationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private ConversationRole role = ConversationRole.MEMBER;

    private Long lastSeenMessageId;

    //private boolean muted = false;

    //private boolean archived = false;

    //private boolean pinned = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    public ConversationMember() {
    }

    public ConversationMember(Long id, Conversation conversation, User user, ConversationRole role, Long lastSeenMessageId, LocalDateTime joinedAt) {
        this.id = id;
        this.conversation = conversation;
        this.user = user;
        this.role = role;
        this.lastSeenMessageId = lastSeenMessageId;
        this.joinedAt = joinedAt;
    }

    public ConversationMember(Conversation conversation, User user, ConversationRole role) {
        this.conversation = conversation;
        this.user = user;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ConversationRole getRole() {
        return role;
    }

    public void setRole(ConversationRole role) {
        this.role = role;
    }

    public Long getLastSeenMessageId() {
        return lastSeenMessageId;
    }

    public void setLastSeenMessageId(Long lastSeenMessageId) {
        this.lastSeenMessageId = lastSeenMessageId;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
