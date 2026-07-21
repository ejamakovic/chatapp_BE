package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {
    Optional<PostAttachment> findByIdAndPostId(Long id, Long postId);
}