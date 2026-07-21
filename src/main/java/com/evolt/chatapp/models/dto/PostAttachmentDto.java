package com.evolt.chatapp.models.dto;

import com.evolt.chatapp.models.PostAttachment;

public class PostAttachmentDto {
    private Long id;
    private String url;
    private String fileType;

    public PostAttachmentDto(Long postId, PostAttachment attachment) {
        this.id = attachment.getId();
        this.url = "/posts/" + postId + "/media/" + attachment.getId();
        this.fileType = attachment.getFileType();
    }

    public Long getId() { return id; }
    public String getUrl() { return url; }
    public String getFileType() { return fileType; }
}