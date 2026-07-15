package com.evolt.chatapp.models.dto;

import com.evolt.chatapp.models.Attachment;
import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.MessageReaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDto {

    private Long id;

    private String content;

    private String status;

    private UserDto sender;

    private Long conversationId;

    private LocalDateTime timestamp;

    private List<AttachmentDto> attachments;

    private List<MessageReactionDto> messageReactions;

    public MessageDto() {
    }

    public MessageDto(Message message) {
        this.id = message.getId();
        this.content = message.getContent();

        this.sender = message.getSender() != null
                ? new UserDto(message.getSender())
                : null;
        this.timestamp = message.getTimestamp();
        List<AttachmentDto> list = new ArrayList<>();
        for (Attachment attachment : message.getAttachments()) {
            AttachmentDto attachmentDTO = new AttachmentDto(attachment);
            list.add(attachmentDTO);
        }
        List<MessageReactionDto> messageReactions = new ArrayList<>();
        for (MessageReaction reaction : message.getMessageReactions()) {
            MessageReactionDto messageReactionDTO = new MessageReactionDto(reaction);
            messageReactions.add(messageReactionDTO);
        }
        this.status = message.getStatus().toString();
        this.attachments = list;
        this.messageReactions = messageReactions;
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

    public UserDto getSender() {
        return sender;
    }

    public void setSender(UserDto sender) {
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

    public List<AttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentDto> attachments) {
        this.attachments = attachments;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<MessageReactionDto> getMessageReactions() {
        return messageReactions;
    }

    public void setMessageReactions(List<MessageReactionDto> messageReactions) {
        this.messageReactions = messageReactions;
    }
}