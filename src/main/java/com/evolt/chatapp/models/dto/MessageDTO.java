package com.evolt.chatapp.models.dto;

import com.evolt.chatapp.models.User;

public class MessageDTO {
    private String content;
    private UserDTO sender;
    private String timestamp;
    private UserDTO receiver;

    public MessageDTO(String content, UserDTO sender) {
        this.content = content;
        this.sender = sender;
    }

    public MessageDTO(UserDTO sender, UserDTO receiver, String content) {
        this.receiver = receiver;
        this.sender = sender;
        this.content = content;
    }

    public MessageDTO(MessageDTO message) {
        this.content = message.getContent();
        this.sender = message.getSender();
        this.receiver = message.getReceiver();
        this.timestamp = message.getTimestamp();
    }

    public MessageDTO() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UserDTO getSender() {
        return sender;
    }

    public void setSender(UserDTO sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public UserDTO getReceiver() {
        return receiver;
    }

    public void setReceiver(UserDTO receiver) {
        this.receiver = receiver;
    }

}
