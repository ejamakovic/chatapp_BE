package com.evolt.chatapp.models.dto;

import java.time.LocalDateTime;

public class ConversationListDto {
    private Long id;
    private String content;
    private Long senderId;
    private String senderUsername;
    private LocalDateTime timestamp;

    public ConversationListDto(Long id, String content, Long senderId, String senderUsername, LocalDateTime timestamp) {
        this.id = id;
        this.content = content;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}