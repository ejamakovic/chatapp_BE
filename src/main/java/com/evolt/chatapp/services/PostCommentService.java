package com.evolt.chatapp.services;

import com.evolt.chatapp.models.CommentReaction;
import com.evolt.chatapp.models.Post;
import com.evolt.chatapp.models.PostComment;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.PostCommentDto;
import com.evolt.chatapp.repositories.CommentReactionRepository;
import com.evolt.chatapp.repositories.PostCommentRepository;
import com.evolt.chatapp.repositories.PostRepository;
import com.evolt.chatapp.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostCommentService {

    private final PostCommentRepository commentRepository;
    private final CommentReactionRepository commentReactionRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostCommentService(
            PostCommentRepository commentRepository,
            CommentReactionRepository commentReactionRepository,
            PostRepository postRepository,
            UserRepository userRepository
    ) {
        this.commentRepository = commentRepository;
        this.commentReactionRepository = commentReactionRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PostCommentDto addComment(Long postId, Long authorId, String content, Long parentCommentId) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent(content.trim());

        if (parentCommentId != null) {
            PostComment parent = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
            if (!parent.getPost().getId().equals(postId)) {
                throw new IllegalArgumentException("Parent comment belongs to a different post");
            }
            comment.setParentComment(parent);
        }

        return toDto(commentRepository.save(comment), authorId);
    }

    @Transactional
    public void deleteComment(Long commentId, Long requesterId) {
        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        boolean isCommentAuthor = comment.getAuthor().getId().equals(requesterId);
        boolean isPostAuthor = comment.getPost().getAuthor().getId().equals(requesterId);

        if (!isCommentAuthor && !isPostAuthor) {
            throw new AccessDeniedException("You cannot delete this comment");
        }

        commentRepository.delete(comment);
    }

    /** Top-level comments for a post: ranked (reactions + reply count, decayed by age), then paginated. */
    public Page<PostCommentDto> getTopLevelComments(Long postId, Long requesterId, int page, int size) {
        return rankAndPaginate(commentRepository.findByPostIdAndParentCommentIsNull(postId), requesterId, page, size);
    }

    public Page<PostCommentDto> getReplies(Long parentCommentId, Long requesterId, int page, int size) {
        return rankAndPaginate(commentRepository.findByParentCommentId(parentCommentId), requesterId, page, size);
    }

    private Page<PostCommentDto> rankAndPaginate(List<PostComment> comments, Long requesterId, int page, int size) {
        if (comments.isEmpty()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), 0);
        }

        List<Long> ids = comments.stream().map(PostComment::getId).toList();
        Map<Long, List<CommentReaction>> reactionsByComment = commentReactionRepository.findByCommentIdIn(ids)
                .stream().collect(Collectors.groupingBy(r -> r.getComment().getId()));

        Map<Long, Long> replyCounts = new HashMap<>();
        for (PostComment c : comments) {
            replyCounts.put(c.getId(), commentRepository.countByParentCommentId(c.getId()));
        }

        List<PostComment> sorted = comments.stream()
                .sorted((a, b) -> Double.compare(score(b, reactionsByComment, replyCounts), score(a, reactionsByComment, replyCounts)))
                .toList();

        List<PostComment> pageContent = RankingUtil.paginate(sorted, page, size);
        List<PostCommentDto> dtos = pageContent.stream()
                .map(c -> toDto(c, requesterId, reactionsByComment.getOrDefault(c.getId(), List.of()), replyCounts.getOrDefault(c.getId(), 0L)))
                .toList();

        return new PageImpl<>(dtos, PageRequest.of(page, size), sorted.size());
    }

    private double score(PostComment c, Map<Long, List<CommentReaction>> reactionsByComment, Map<Long, Long> replyCounts) {
        long reactionCount = reactionsByComment.getOrDefault(c.getId(), List.of()).size();
        long replyCount = replyCounts.getOrDefault(c.getId(), 0L);
        return RankingUtil.commentScore(reactionCount, replyCount, c.getCreatedAt());
    }

    private PostCommentDto toDto(PostComment c, Long requesterId) {
        List<CommentReaction> reactions = commentReactionRepository.findByCommentId(c.getId());
        long replyCount = commentRepository.countByParentCommentId(c.getId());
        return toDto(c, requesterId, reactions, replyCount);
    }

    private PostCommentDto toDto(PostComment c, Long requesterId, List<CommentReaction> reactions, long replyCount) {
        Map<String, Long> counts = reactions.stream().collect(Collectors.groupingBy(CommentReaction::getEmoji, Collectors.counting()));
        String myReaction = reactions.stream()
                .filter(r -> r.getUser().getId().equals(requesterId))
                .map(CommentReaction::getEmoji).findFirst().orElse(null);

        return new PostCommentDto(
                c.getId(), c.getPost().getId(),
                c.getParentComment() != null ? c.getParentComment().getId() : null,
                c.getAuthor().getId(), c.getAuthor().getUsername(), c.getAuthor().getAvatarUrl(),
                c.getContent(), c.getCreatedAt(), replyCount, counts, myReaction
        );
    }
}