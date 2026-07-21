package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostIdAndParentCommentIsNull(Long postId);
    List<PostComment> findByParentCommentId(Long parentCommentId);
    long countByPostId(Long postId);
    long countByParentCommentId(Long parentCommentId);
}