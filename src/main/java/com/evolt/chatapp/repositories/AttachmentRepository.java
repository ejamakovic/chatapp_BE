package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    // Verifies if the user is a member of the conversation that owns this attachment
    @Query("SELECT a FROM Attachment a " +
            "JOIN a.message m " +
            "JOIN m.conversation c " +
            "JOIN c.members member " +
            "WHERE a.id = :attachmentId AND member.user.id = :userId")
    Optional<Attachment> findByIdAndUserId(@Param("attachmentId") Long attachmentId,
                                           @Param("userId") Long userId);
}