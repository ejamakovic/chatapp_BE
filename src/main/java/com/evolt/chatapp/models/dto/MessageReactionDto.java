package com.evolt.chatapp.models.dto;

import com.evolt.chatapp.models.MessageReaction;
import java.time.LocalDateTime;

public class MessageReactionDto {
    private Long id;
    private Long messageId;
    private Long conversationId;
    private UserDto user;
    private String emoji;
    private LocalDateTime timestamp;

    public MessageReactionDto(MessageReaction reaction) {
        this.id = reaction.getId();
        this.messageId = reaction.getMessage().getId();
        this.conversationId = reaction.getMessage().getConversation().getId();
        this.user = new UserDto(reaction.getUser());
        this.emoji = reaction.getEmoji();
        this.timestamp = reaction.getCreatedAt();
    }

    public Long getId() { return id; }
    public Long getMessageId() { return messageId; }
    public Long getConversationId() { return conversationId; }
    public UserDto getUser() { return user; }
    public String getEmoji() { return emoji; }
    public LocalDateTime getTimestamp() { return timestamp; }
}