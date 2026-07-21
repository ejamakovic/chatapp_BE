package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {

    @Query("""
        SELECT r FROM MessageReaction r
        JOIN FETCH r.user
        WHERE r.message.id = :messageId
        ORDER BY r.createdAt ASC
    """)
    List<MessageReaction> findByMessageId(@Param("messageId") Long messageId);

    Optional<MessageReaction> findByMessageIdAndUserId(Long messageId, Long userId);
}