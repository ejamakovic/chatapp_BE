package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Post;
import com.evolt.chatapp.models.PostComment;
import com.evolt.chatapp.models.PostLike;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.PostCommentDto;
import com.evolt.chatapp.models.dto.PostDto;
import com.evolt.chatapp.models.dto.UpdatePostRequest;
import com.evolt.chatapp.models.enums.PostPrivacy;
import com.evolt.chatapp.repositories.FriendshipRepository;
import com.evolt.chatapp.repositories.PostCommentRepository;
import com.evolt.chatapp.repositories.PostLikeRepository;
import com.evolt.chatapp.repositories.PostRepository;
import com.evolt.chatapp.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public PostService(
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            PostCommentRepository postCommentRepository,
            UserRepository userRepository,
            FriendshipRepository friendshipRepository
    ) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.postCommentRepository = postCommentRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    // ── Create ───────────────────────────────────────────────────────────────

    @Transactional
    public PostDto createPost(Long authorId, String content, String privacyRaw, MultipartFile image) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Post content cannot be empty");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        Post post = new Post();
        post.setAuthor(author);
        post.setContent(content.trim());
        post.setPrivacy(parsePrivacy(privacyRaw));

        if (image != null && !image.isEmpty()) {
            post.setImageUrl(storeImage(image));
        }

        Post saved = postRepository.save(post);
        return toDto(saved, authorId);
    }

    private String storeImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Post image must be an image file");
        }

        try {
            Path uploadDir = Paths.get("uploads", "posts");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path dest = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            return "posts/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store post image", e);
        }
    }

    private PostPrivacy parsePrivacy(String raw) {
        if (raw == null) return PostPrivacy.PUBLIC;
        try {
            return PostPrivacy.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return PostPrivacy.PUBLIC;
        }
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    public Page<PostDto> getUserPosts(Long profileUserId, Long requesterId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postRepository.findVisiblePostsByAuthor(profileUserId, requesterId, pageable);
        return posts.map(p -> toDto(p, requesterId));
    }

    public Post findByIdCheckingVisibility(Long postId, Long requesterId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!isVisible(post, requesterId)) {
            throw new AccessDeniedException("You cannot view this post");
        }
        return post;
    }

    private boolean isVisible(Post post, Long requesterId) {
        if (post.getAuthor().getId().equals(requesterId)) return true;
        if (post.getPrivacy() == PostPrivacy.PUBLIC) return true;
        if (post.getPrivacy() == PostPrivacy.PRIVATE) return false;
        return friendshipRepository.existsAcceptedFriendshipBetween(post.getAuthor().getId(), requesterId);
    }

    // ── Update / Delete ──────────────────────────────────────────────────────

    @Transactional
    public PostDto updatePost(Long postId, Long requesterId, UpdatePostRequest body) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!post.getAuthor().getId().equals(requesterId)) {
            throw new AccessDeniedException("You can only edit your own posts");
        }

        if (body.getContent() != null) {
            if (body.getContent().isBlank()) {
                throw new IllegalArgumentException("Content cannot be empty");
            }
            post.setContent(body.getContent().trim());
        }

        if (body.getPrivacy() != null) {
            post.setPrivacy(parsePrivacy(body.getPrivacy()));
        }

        return toDto(postRepository.save(post), requesterId);
    }

    @Transactional
    public void deletePost(Long postId, Long requesterId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!post.getAuthor().getId().equals(requesterId)) {
            throw new AccessDeniedException("You can only delete your own posts");
        }

        postRepository.delete(post);
    }

    // ── Likes ────────────────────────────────────────────────────────────────

    @Transactional
    public void likePost(Long postId, Long userId) {
        Post post = findByIdCheckingVisibility(postId, userId);

        if (postLikeRepository.existsByPostIdAndUserId(post.getId(), userId)) {
            return; // already liked — idempotent
        }

        User user = userRepository.getReferenceById(userId);
        PostLike like = new PostLike();
        like.setPost(post);
        like.setUser(user);
        postLikeRepository.save(like);
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
        postLikeRepository.deleteByPostIdAndUserId(postId, userId);
    }

    // ── Comments ─────────────────────────────────────────────────────────────

    public List<PostCommentDto> getComments(Long postId, Long requesterId) {
        Post post = findByIdCheckingVisibility(postId, requesterId);
        return postCommentRepository.findByPostIdOrderByCreatedAtAsc(post.getId())
                .stream()
                .map(this::toCommentDto)
                .toList();
    }

    @Transactional
    public PostCommentDto addComment(Long postId, Long authorId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }

        Post post = findByIdCheckingVisibility(postId, authorId);
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent(content.trim());

        return toCommentDto(postCommentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId, Long requesterId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        boolean isCommentAuthor = comment.getAuthor().getId().equals(requesterId);
        boolean isPostAuthor = comment.getPost().getAuthor().getId().equals(requesterId);

        if (!isCommentAuthor && !isPostAuthor) {
            throw new AccessDeniedException("You cannot delete this comment");
        }

        postCommentRepository.delete(comment);
    }

    // ── Mapping ──────────────────────────────────────────────────────────────

    private PostDto toDto(Post post, Long requesterId) {
        long likeCount = postLikeRepository.countByPostId(post.getId());
        long commentCount = postCommentRepository.countByPostId(post.getId());
        boolean likedByMe = postLikeRepository.existsByPostIdAndUserId(post.getId(), requesterId);
        String publicImageUrl = post.getImageUrl() != null ? "/posts/" + post.getId() + "/image" : null;

        return new PostDto(
                post.getId(),
                post.getAuthor().getId(),
                post.getAuthor().getUsername(),
                post.getContent(),
                publicImageUrl,
                post.getPrivacy().name(),
                post.getCreatedAt(),
                likeCount,
                commentCount,
                likedByMe
        );
    }

    private PostCommentDto toCommentDto(PostComment comment) {
        return new PostCommentDto(
                comment.getId(),
                comment.getPost().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getUsername(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}