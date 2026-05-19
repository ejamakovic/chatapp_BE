package com.evolt.chatapp.models;

import com.evolt.chatapp.models.enums.MessageStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "messages",
        indexes = {
                @Index(name = "idx_message_conversation",
                        columnList = "conversation_id"),

                @Index(name = "idx_message_sender",
                        columnList = "sender_id"),

                @Index(name = "idx_message_timestamp",
                        columnList = "timestamp"),

                @Index(name = "idx_conversation_timestamp",
                        columnList = "conversation_id,timestamp")
        }
)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.SENT;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @OneToMany(
            mappedBy = "message",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Attachment> attachments = new ArrayList<>();

    public Message(User sender, Conversation conversation, String content) {
        this.sender = sender;
        this.conversation = conversation;
        this.content = content;
    }

    public Message(
            Long id,
            Conversation conversation,
            User sender,
            String content,
            MessageStatus status,
            LocalDateTime timestamp,
            List<Attachment> attachments) {
        this.id = id;
        this.conversation = conversation;
        this.sender = sender;
        this.content = content;
        this.status = status;
        this.timestamp = timestamp;
        this.attachments = attachments;
    }

    public Message(
            Conversation conversation,
            User sender, String content,
            MessageStatus status) {
        this.conversation = conversation;
        this.sender = sender;
        this.content = content;
        this.status = status;
    }

    public Message(Conversation conversation, User sender, String content) {
        this.conversation = conversation;
        this.sender = sender;
        this.content = content;
    }

    public Message() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}
