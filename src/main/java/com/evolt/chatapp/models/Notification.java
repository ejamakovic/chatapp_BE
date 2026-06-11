package com.evolt.chatapp.models;

import com.evolt.chatapp.models.enums.NotificationStatus;
import com.evolt.chatapp.models.enums.NotificationType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notification_user", columnList = "recipient_id"),
                @Index(name = "idx_notification_status", columnList = "status"),
                @Index(name = "idx_notification_created", columnList = "created_at")
        }
)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false)
    private Long referenceId;

    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public Notification() {
    }

    public Notification(User recipient, NotificationType type, Long referenceId, String content, NotificationStatus status, LocalDateTime timestamp) {
        this.recipient = recipient;
        this.type = type;
        this.referenceId = referenceId;
        this.content = content;
        this.status = status;
        this.timestamp = timestamp;
    }

    public Notification(Long id, User recipient, NotificationType type, Long referenceId, String content, NotificationStatus status, LocalDateTime timestamp) {
        this.id = id;
        this.recipient = recipient;
        this.type = type;
        this.referenceId = referenceId;
        this.content = content;
        this.status = status;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User user) {
        this.recipient = user;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime createdAt) {
        this.timestamp = createdAt;
    }
}