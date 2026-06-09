package com.evolt.chatapp.models.dto;

import com.evolt.chatapp.models.Attachment;

public class AttachmentDto {
    private Long id;
    private String fileUrl;
    private String fileType;

    public AttachmentDto(Long id, String fileUrl, String fileType) {
        this.id = id;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
    }

    public AttachmentDto(Attachment attachment) {
        this.id = attachment.getId();
        this.fileUrl = attachment.getFileUrl();
        this.fileType = attachment.getFileType();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
