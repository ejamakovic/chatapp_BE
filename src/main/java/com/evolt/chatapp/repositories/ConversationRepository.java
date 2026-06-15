package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.Conversation;
import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.dto.ConversationListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
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
        s.id,
        s.username,
        m.timestamp,
        (SELECT COUNT(msg) FROM Message msg 
         WHERE msg.conversation = c 
           AND msg.id > cm.lastSeenMessageId 
           AND msg.sender.id <> :userId)
    )
    FROM ConversationMember cm
    JOIN cm.conversation c
    LEFT JOIN c.lastMessage m
    LEFT JOIN m.sender s
    WHERE cm.user.id = :userId
    ORDER BY COALESCE(m.timestamp, c.createdAt) DESC
""")
    Page<ConversationListDto> findUserConversations(
            @Param("userId") Long userId,
            Pageable pageable
    );

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
    Optional<Conversation> findPrivateConversation(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    @Modifying
    @Query("""
UPDATE Conversation c
SET c.lastMessage = :message
WHERE c.id = :conversationId
""")
    void updateLastMessage(
            @Param("conversationId") Long conversationId,
            @Param("message") Message message
    );


}
