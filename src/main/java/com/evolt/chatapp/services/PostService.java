package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Post;
import com.evolt.chatapp.models.PostAttachment;
import com.evolt.chatapp.models.PostReaction;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.PostAttachmentDto;
import com.evolt.chatapp.models.dto.PostDto;
import com.evolt.chatapp.models.dto.UpdatePostRequest;
import com.evolt.chatapp.models.enums.PostPrivacy;
import com.evolt.chatapp.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostAttachmentRepository postAttachmentRepository;
    private final PostReactionRepository postReactionRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public PostService(
            PostRepository postRepository,
            PostAttachmentRepository postAttachmentRepository,
            PostReactionRepository postReactionRepository,
            PostCommentRepository postCommentRepository,
            UserRepository userRepository,
            FriendshipRepository friendshipRepository
    ) {
        this.postRepository = postRepository;
        this.postAttachmentRepository = postAttachmentRepository;
        this.postReactionRepository = postReactionRepository;
        this.postCommentRepository = postCommentRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    // ── Create ───────────────────────────────────────────────────────────────

    @Transactional
    public PostDto createPost(Long authorId, String content, String privacyRaw, List<MultipartFile> media) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Post content cannot be empty");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        Post post = new Post();
        post.setAuthor(author);
        post.setContent(content.trim());
        post.setPrivacy(parsePrivacy(privacyRaw));
        Post saved = postRepository.save(post);

        if (media != null) {
            int order = 0;
            for (MultipartFile file : media) {
                if (file == null || file.isEmpty()) continue;
                String path = storeMedia(file);
                PostAttachment attachment = new PostAttachment(saved, path, file.getContentType(), order++);
                postAttachmentRepository.save(attachment);
                saved.getMedia().add(attachment);
            }
        }

        return toDto(saved, authorId, requesterFriendIds(authorId));
    }

    private String storeMedia(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.startsWith("image/") || contentType.startsWith("video/"))) {
            throw new IllegalArgumentException("Post media must be an image or video file");
        }
        try {
            Path uploadDir = Paths.get("uploads", "posts");
            if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path dest = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            return "posts/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store post media", e);
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
        Set<Long> friendIds = requesterFriendIds(requesterId);
        return posts.map(p -> toDto(p, requesterId, friendIds));
    }

    /**
     * Global feed: candidate posts from the last 30 days visible to the requester
     * (public + own + friends' FRIENDS-only posts), ranked "hot" and friends-boosted.
     * See RankingUtil.postScore.
     */
    public Page<PostDto> getFeed(Long requesterId, int page, int size) {
        List<Long> friendIdList = friendshipRepository.findFriendIds(requesterId);
        Set<Long> friendIds = new HashSet<>(friendIdList);

        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<Post> candidates = postRepository.findFeedCandidates(
                requesterId, friendIdList.isEmpty() ? List.of(-1L) : friendIdList, since);

        if (candidates.isEmpty()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), 0);
        }

        List<Long> postIds = candidates.stream().map(Post::getId).toList();
        Map<Long, Long> reactionCounts = postReactionRepository.findByPostIdIn(postIds).stream()
                .collect(Collectors.groupingBy(r -> r.getPost().getId(), Collectors.counting()));

        List<Post> sorted = candidates.stream()
                .sorted((a, b) -> Double.compare(
                        RankingUtil.postScore(reactionCounts.getOrDefault(b.getId(), 0L), postCommentRepository.countByPostId(b.getId()), b.getCreatedAt(), friendIds.contains(b.getAuthor().getId())),
                        RankingUtil.postScore(reactionCounts.getOrDefault(a.getId(), 0L), postCommentRepository.countByPostId(a.getId()), a.getCreatedAt(), friendIds.contains(a.getAuthor().getId()))
                ))
                .toList();

        List<Post> pageContent = RankingUtil.paginate(sorted, page, size);
        List<PostDto> dtos = pageContent.stream().map(p -> toDto(p, requesterId, friendIds)).toList();

        return new PageImpl<>(dtos, PageRequest.of(page, size), sorted.size());
    }

    public Post findByIdCheckingVisibility(Long postId, Long requesterId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        if (!isVisible(post, requesterId)) {
            throw new AccessDeniedException("You cannot view this post");
        }
        return post;
    }

    public PostAttachment findMediaCheckingVisibility(Long postId, Long mediaId, Long requesterId) {
        findByIdCheckingVisibility(postId, requesterId);
        return postAttachmentRepository.findByIdAndPostId(mediaId, postId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));
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
            if (body.getContent().isBlank()) throw new IllegalArgumentException("Content cannot be empty");
            post.setContent(body.getContent().trim());
        }
        if (body.getPrivacy() != null) post.setPrivacy(parsePrivacy(body.getPrivacy()));

        return toDto(postRepository.save(post), requesterId, requesterFriendIds(requesterId));
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

    // ── Mapping ──────────────────────────────────────────────────────────────

    private Set<Long> requesterFriendIds(Long requesterId) {
        return new HashSet<>(friendshipRepository.findFriendIds(requesterId));
    }

    private PostDto toDto(Post post, Long requesterId, Set<Long> friendIds) {
        long commentCount = postCommentRepository.countByPostId(post.getId());
        List<PostReaction> reactions = postReactionRepository.findByPostId(post.getId());

        Map<String, Long> reactionCounts = reactions.stream()
                .collect(Collectors.groupingBy(PostReaction::getEmoji, Collectors.counting()));
        String myReaction = reactions.stream()
                .filter(r -> r.getUser().getId().equals(requesterId))
                .map(PostReaction::getEmoji).findFirst().orElse(null);

        List<PostAttachmentDto> media = post.getMedia().stream()
                .map(a -> new PostAttachmentDto(post.getId(), a)).toList();

        return new PostDto(
                post.getId(), post.getAuthor().getId(), post.getAuthor().getUsername(), post.getAuthor().getAvatarUrl(),
                post.getContent(), media, post.getPrivacy().name(), post.getCreatedAt(),
                commentCount, reactionCounts, reactions.size(), myReaction,
                friendIds.contains(post.getAuthor().getId())
        );
    }
}