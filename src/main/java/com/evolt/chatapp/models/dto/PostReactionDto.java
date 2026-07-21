package com.evolt.chatapp.models.dto;

import com.evolt.chatapp.models.PostReaction;
import java.time.LocalDateTime;

public class PostReactionDto {
    private Long id;
    private Long postId;
    private UserDto user;
    private String emoji;
    private LocalDateTime timestamp;

    public PostReactionDto(PostReaction reaction) {
        this.id = reaction.getId();
        this.postId = reaction.getPost().getId();
        this.user = new UserDto(reaction.getUser());
        this.emoji = reaction.getEmoji();
        this.timestamp = reaction.getCreatedAt();
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public UserDto getUser() { return user; }
    public String getEmoji() { return emoji; }
    public LocalDateTime getTimestamp() { return timestamp; }
}