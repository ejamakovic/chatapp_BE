package com.evolt.chatapp.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "conversations",
        indexes = {
                @Index(name = "idx_conversation_created", columnList = "createdAt")
        }
)
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationType type;

    @OneToMany(
            mappedBy = "conversationMembers",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ConversationMember> members;


    private String name;

    private String imageUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
