package com.evolt.chatapp.services;

import com.evolt.chatapp.models.CommentReaction;
import com.evolt.chatapp.models.PostComment;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.CommentReactionDto;
import com.evolt.chatapp.models.enums.AllowedReactions;
import com.evolt.chatapp.repositories.CommentReactionRepository;
import com.evolt.chatapp.repositories.PostCommentRepository;
import com.evolt.chatapp.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentReactionService {

    private final CommentReactionRepository reactionRepository;
    private final PostCommentRepository commentRepository;
    private final UserRepository userRepository;

    public CommentReactionService(CommentReactionRepository reactionRepository, PostCommentRepository commentRepository, UserRepository userRepository) {
        this.reactionRepository = reactionRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommentReactionDto setReaction(Long commentId, Long userId, String emoji) {
        if (!AllowedReactions.isAllowed(emoji)) {
            throw new IllegalArgumentException("Unsupported reaction emoji");
        }

        Optional<CommentReaction> existing = reactionRepository.findByCommentIdAndUserId(commentId, userId);

        if (existing.isPresent() && existing.get().getEmoji().equals(emoji)) {
            reactionRepository.delete(existing.get());
            return null;
        }

        CommentReaction reaction = existing.orElseGet(() -> {
            PostComment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return new CommentReaction(comment, user, emoji);
        });
        reaction.setEmoji(emoji);
        reaction.setCreatedAt(LocalDateTime.now());

        return new CommentReactionDto(reactionRepository.save(reaction));
    }

    public List<CommentReactionDto> getReactions(Long commentId) {
        return reactionRepository.findByCommentId(commentId).stream().map(CommentReactionDto::new).toList();
    }
}