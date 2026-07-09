package com.evolt.chatapp.models.dto;

import java.time.LocalDateTime;

public class PostCommentDto {
    private Long id;
    private Long postId;
    private Long authorId;
    private String authorUsername;
    private String content;
    private LocalDateTime createdAt;

    public PostCommentDto(Long id, Long postId, Long authorId, String authorUsername,
                          String content, LocalDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getAuthorId() { return authorId; }
    public String getAuthorUsername() { return authorUsername; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}