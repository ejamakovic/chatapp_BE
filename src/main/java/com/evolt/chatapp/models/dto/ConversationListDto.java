package com.evolt.chatapp.models.dto;

import java.time.LocalDateTime;

public class ConversationListDto {
    private Long id;
    private String name;
    private String imageUrl;
    private String lastMessage;
    private String senderUsername;
    private LocalDateTime timestamp;
    private Long unreadCount;

    public ConversationListDto() {
    }

    public ConversationListDto(
            Long id,
            String name,
            String imageUrl,
            String lastMessage,
            String senderUsername,
            LocalDateTime timestamp,
            Long unreadCount
    ) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.lastMessage = lastMessage;
        this.senderUsername = senderUsername;
        this.unreadCount = unreadCount;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}