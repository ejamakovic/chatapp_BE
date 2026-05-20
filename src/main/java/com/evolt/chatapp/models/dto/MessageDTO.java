package com.evolt.chatapp.models.dto;

import com.evolt.chatapp.models.Attachment;
import com.evolt.chatapp.models.Message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
public class MessageDTO {

    private Long id;

    private String content;

    private UserDTO sender;

    private Long conversationId;

    private LocalDateTime timestamp;

    private List<AttachmentDTO> attachments;

    public MessageDTO() {
    }

    public MessageDTO(Message message) {
        this.id = message.getId();
        this.content = message.getContent();

        this.sender = message.getSender() != null
                ? new UserDTO(message.getSender())
                : null;
        this.timestamp = message.getTimestamp();
        List<AttachmentDTO> list = new ArrayList<>();
        for (Attachment attachment : message.getAttachments()) {
            AttachmentDTO attachmentDTO = new AttachmentDTO(attachment);
            list.add(attachmentDTO);
        }
        this.attachments = list;
        this.conversationId = message.getConversation().getId();
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

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<AttachmentDTO> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentDTO> attachments) {
        this.attachments = attachments;
    }
}