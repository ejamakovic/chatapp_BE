package com.evolt.chatapp.models;

import jakarta.persistence.*;

@Entity
@Table(
        name = "post_attachments",
        indexes = { @Index(name = "idx_post_attachment_post", columnList = "post_id") }
)
public class PostAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /** Relative path on disk, e.g. "posts/uuid.jpg". Served via /posts/{postId}/media/{id}. */
    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    public PostAttachment() {}

    public PostAttachment(Post post, String filePath, String fileType, int orderIndex) {
        this.post = post;
        this.filePath = filePath;
        this.fileType = fileType;
        this.orderIndex = orderIndex;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}