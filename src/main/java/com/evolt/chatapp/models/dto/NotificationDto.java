package com.evolt.chatapp.models.dto;

import com.evolt.chatapp.models.Notification;

import java.time.LocalDateTime;

public class NotificationDto {

    private Long id;
    private UserDto recipient;
    private Long referenceId;
    private String notificationType;
    private String status;
    private String content;
    private LocalDateTime timestamp;

    public NotificationDto(Notification notification) {
        this.id = notification.getId();
        this.recipient = new UserDto(notification.getRecipient());
        this.referenceId = notification.getReferenceId();
        this.notificationType = String.valueOf(notification.getType());
        this.status = String.valueOf(notification.getStatus());
        this.content = notification.getContent();
        this.timestamp = notification.getTimestamp();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserDto getRecipient() {
        return recipient;
    }

    public void setRecipient(UserDto receiver) {
        this.recipient = receiver;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
