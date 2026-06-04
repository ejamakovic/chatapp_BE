package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.Conversation;
import com.evolt.chatapp.models.dto.ConversationListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("""
    SELECT DISTINCT c FROM Conversation c
    WHERE c.type = "GLOBAL"
""")
    Conversation findGlobalConversation();

    @Query("""
    SELECT new com.evolt.chatapp.models.dto.ConversationListDto(
        c.id,
        m.content,
        m.sender.id,
        m.sender.username,
        m.timestamp
    )
    FROM ConversationMember cm
    JOIN cm.conversation c
    LEFT JOIN Message m ON m.conversation.id = c.id
        AND m.id = (SELECT MAX(m2.id) FROM Message m2 WHERE m2.conversation.id = c.id)
    WHERE cm.user.id = :userId
    ORDER BY COALESCE(m.timestamp, c.createdAt) DESC
""")
    Page<ConversationListDto> findUserConversations(Long userId, Pageable pageable);

    @Query("""
    SELECT cm1.conversation
    FROM ConversationMember cm1
    JOIN ConversationMember cm2 ON cm1.conversation.id = cm2.conversation.id
    WHERE cm1.user.id = :senderId
      AND cm2.user.id = :receiverId
      AND (
          SELECT COUNT(sub_cm)
          FROM ConversationMember sub_cm
          WHERE sub_cm.conversation.id = cm1.conversation.id
      ) = 2
""")
    Optional<Conversation> findPrivateConversation(Long senderId, Long receiverId);
}
