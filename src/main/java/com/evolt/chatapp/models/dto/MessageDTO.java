package com.evolt.chatapp.models.dto;

import com.evolt.chatapp.models.Attachment;
import com.evolt.chatapp.models.Message;

import java.time.LocalDateTime;
import java.util.List;

public class MessageDTO {

    private Long id;

    private String content;

    private UserDTO sender;

    private UserDTO receiver;

    private LocalDateTime timestamp;

    private List<Attachment> attachments;

    public MessageDTO() {
    }

    public MessageDTO(String content, UserDTO sender) {
        this.content = content;
        this.sender = sender;
    }

    public MessageDTO(UserDTO sender, UserDTO receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    public MessageDTO(Message message) {

        this.id = message.getId();

        this.content = message.getContent();

        this.sender = message.getSender() != null
                ? new UserDTO(message.getSender())
                : null;

        this.receiver = message.getReceiver() != null
                ? new UserDTO(message.getReceiver())
                : null;

        this.timestamp = message.getTimestamp();

        this.attachments = message.getAttachments();
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

    public UserDTO getSender() {
        return sender;
    }

    public void setSender(UserDTO sender) {
        this.sender = sender;
    }

    public UserDTO getReceiver() {
        return receiver;
    }

    public void setReceiver(UserDTO receiver) {
        this.receiver = receiver;
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