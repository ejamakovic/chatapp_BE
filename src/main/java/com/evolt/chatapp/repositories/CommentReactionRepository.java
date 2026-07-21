package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.CommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {
    List<CommentReaction> findByCommentId(Long commentId);
    List<CommentReaction> findByCommentIdIn(List<Long> commentIds);
    Optional<CommentReaction> findByCommentIdAndUserId(Long commentId, Long userId);
}