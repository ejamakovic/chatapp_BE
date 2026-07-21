package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Post;
import com.evolt.chatapp.models.PostReaction;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.PostReactionDto;
import com.evolt.chatapp.models.enums.AllowedReactions;
import com.evolt.chatapp.repositories.PostReactionRepository;
import com.evolt.chatapp.repositories.PostRepository;
import com.evolt.chatapp.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PostReactionService {

    private final PostReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostReactionService(PostReactionRepository reactionRepository, PostRepository postRepository, UserRepository userRepository) {
        this.reactionRepository = reactionRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PostReactionDto setReaction(Long postId, Long userId, String emoji) {
        if (!AllowedReactions.isAllowed(emoji)) {
            throw new IllegalArgumentException("Unsupported reaction emoji");
        }

        Optional<PostReaction> existing = reactionRepository.findByPostIdAndUserId(postId, userId);

        if (existing.isPresent() && existing.get().getEmoji().equals(emoji)) {
            reactionRepository.delete(existing.get());
            return null;
        }

        PostReaction reaction = existing.orElseGet(() -> {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("Post not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return new PostReaction(post, user, emoji);
        });
        reaction.setEmoji(emoji);
        reaction.setCreatedAt(LocalDateTime.now());

        return new PostReactionDto(reactionRepository.save(reaction));
    }

    public List<PostReactionDto> getReactions(Long postId) {
        return reactionRepository.findByPostId(postId).stream().map(PostReactionDto::new).toList();
    }
}