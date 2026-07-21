package com.evolt.chatapp.models.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class PostDto {
    private Long id;
    private Long authorId;
    private String authorUsername;
    private String authorAvatarUrl;
    private String content;
    private List<PostAttachmentDto> media;
    private String privacy;
    private LocalDateTime createdAt;
    private long commentCount;
    private Map<String, Long> reactionCounts;
    private long reactionCount;
    private String myReaction;
    private boolean isFriend;

    public PostDto(Long id, Long authorId, String authorUsername, String authorAvatarUrl,
                   String content, List<PostAttachmentDto> media, String privacy,
                   LocalDateTime createdAt, long commentCount, Map<String, Long> reactionCounts,
                   long reactionCount, String myReaction, boolean isFriend) {
        this.id = id;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.authorAvatarUrl = authorAvatarUrl;
        this.content = content;
        this.media = media;
        this.privacy = privacy;
        this.createdAt = createdAt;
        this.commentCount = commentCount;
        this.reactionCounts = reactionCounts;
        this.reactionCount = reactionCount;
        this.myReaction = myReaction;
        this.isFriend = isFriend;
    }

    public Long getId() { return id; }
    public Long getAuthorId() { return authorId; }
    public String getAuthorUsername() { return authorUsername; }
    public String getAuthorAvatarUrl() { return authorAvatarUrl; }
    public String getContent() { return content; }
    public List<PostAttachmentDto> getMedia() { return media; }
    public String getPrivacy() { return privacy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public long getCommentCount() { return commentCount; }
    public Map<String, Long> getReactionCounts() { return reactionCounts; }
    public long getReactionCount() { return reactionCount; }
    public String getMyReaction() { return myReaction; }
    public boolean isFriend() { return isFriend; }
}