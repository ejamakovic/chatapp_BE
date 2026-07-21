package com.evolt.chatapp.models.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class PostCommentDto {
    private Long id;
    private Long postId;
    private Long parentCommentId;
    private Long authorId;
    private String authorUsername;
    private String authorAvatarUrl;
    private String content;
    private LocalDateTime createdAt;
    private long replyCount;
    private Map<String, Long> reactionCounts;
    private String myReaction;

    public PostCommentDto(Long id, Long postId, Long parentCommentId, Long authorId,
                          String authorUsername, String authorAvatarUrl, String content,
                          LocalDateTime createdAt, long replyCount,
                          Map<String, Long> reactionCounts, String myReaction) {
        this.id = id;
        this.postId = postId;
        this.parentCommentId = parentCommentId;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.authorAvatarUrl = authorAvatarUrl;
        this.content = content;
        this.createdAt = createdAt;
        this.replyCount = replyCount;
        this.reactionCounts = reactionCounts;
        this.myReaction = myReaction;
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getParentCommentId() { return parentCommentId; }
    public Long getAuthorId() { return authorId; }
    public String getAuthorUsername() { return authorUsername; }
    public String getAuthorAvatarUrl() { return authorAvatarUrl; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public long getReplyCount() { return replyCount; }
    public Map<String, Long> getReactionCounts() { return reactionCounts; }
    public String getMyReaction() { return myReaction; }
}