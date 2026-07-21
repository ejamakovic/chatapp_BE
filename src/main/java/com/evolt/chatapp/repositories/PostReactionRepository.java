package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
    List<PostReaction> findByPostId(Long postId);
    List<PostReaction> findByPostIdIn(List<Long> postIds);
    Optional<PostReaction> findByPostIdAndUserId(Long postId, Long userId);
}