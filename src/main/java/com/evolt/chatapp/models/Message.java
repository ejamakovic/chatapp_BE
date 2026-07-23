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
                        columnList = "conversation_id,timestamp"),
                @Index(
                        name = "idx_conversation_id",
                        columnList = "conversation_id,id"
                )
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

    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY)
    private List<Attachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY)
    private List<MessageReaction> messageReactions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id")
    private Message replyTo;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;
    private LocalDateTime deletedAt;
    private LocalDateTime editedAt;

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

    public Message(User sender, Conversation conversation, String content, Message replyTo) {
        this.conversation = conversation;
        this.sender = sender;
        this.content = content;
        this.replyTo = replyTo;
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

    public List<MessageReaction> getMessageReactions() {
        return messageReactions;
    }

    public void setMessageReactions(List<MessageReaction> messageReactions) {
        this.messageReactions = messageReactions;
    }

    public Message getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(Message replyTo) {
        this.replyTo = replyTo;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public LocalDateTime getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(LocalDateTime editedAt) {
        this.editedAt = editedAt;
    }
}
