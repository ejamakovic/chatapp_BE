package com.evolt.chatapp.models.dto;

import java.time.LocalDateTime;

public class ConversationListDto {
    private Long id;
    private String lastMessage;
    private Long senderId;
    private String senderUsername;
    private LocalDateTime timestamp;
    private Long unreadCount = 0L;

    public ConversationListDto() {
    }

    public ConversationListDto(
            Long id,
            String lastMessage,
            Long senderId,
            String senderUsername,
            LocalDateTime timestamp
    ) {
        this.id = id;
        this.lastMessage = lastMessage;
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

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
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

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }
}