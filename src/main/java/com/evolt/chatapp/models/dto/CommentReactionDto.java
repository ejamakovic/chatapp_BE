package com.evolt.chatapp.models.dto;

import com.evolt.chatapp.models.CommentReaction;
import java.time.LocalDateTime;

public class CommentReactionDto {
    private Long id;
    private Long commentId;
    private Long postId;
    private UserDto user;
    private String emoji;
    private LocalDateTime timestamp;

    public CommentReactionDto(CommentReaction reaction) {
        this.id = reaction.getId();
        this.commentId = reaction.getComment().getId();
        this.postId = reaction.getComment().getPost().getId();
        this.user = new UserDto(reaction.getUser());
        this.emoji = reaction.getEmoji();
        this.timestamp = reaction.getCreatedAt();
    }

    public Long getId() { return id; }
    public Long getCommentId() { return commentId; }
    public Long getPostId() { return postId; }
    public UserDto getUser() { return user; }
    public String getEmoji() { return emoji; }
    public LocalDateTime getTimestamp() { return timestamp; }
}