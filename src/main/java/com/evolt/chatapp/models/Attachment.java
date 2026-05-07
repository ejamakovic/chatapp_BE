package com.evolt.chatapp.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(
        name = "attachments",
        indexes = {
                @Index(name = "idx_message_id", columnList = "message_id")
        }
)
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileUrl;

    private String fileType;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "message_id")
    private Message message;

    public Attachment(Long id, String fileUrl, String fileType, Message message) {
        this.id = id;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.message = message;
    }
    public Attachment() {}

    public Attachment(String fileUrl, String fileType, Message message) {
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.message = message;
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

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
