package com.evolt.chatapp.models.dto;

import java.time.LocalDateTime;

public class PostDto {
    private Long id;
    private Long authorId;
    private String authorUsername;
    private String content;
    private String imageUrl;
    private String privacy;
    private LocalDateTime createdAt;
    private long likeCount;
    private long commentCount;
    private boolean likedByCurrentUser;

    public PostDto(Long id, Long authorId, String authorUsername, String content, String imageUrl,
                   String privacy, LocalDateTime createdAt, long likeCount, long commentCount,
                   boolean likedByCurrentUser) {
        this.id = id;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.content = content;
        this.imageUrl = imageUrl;
        this.privacy = privacy;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.likedByCurrentUser = likedByCurrentUser;
    }

    public Long getId() { return id; }
    public Long getAuthorId() { return authorId; }
    public String getAuthorUsername() { return authorUsername; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public String getPrivacy() { return privacy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public long getLikeCount() { return likeCount; }
    public long getCommentCount() { return commentCount; }
    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
}