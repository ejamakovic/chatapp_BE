package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {

    @Query("""
    SELECT DISTINCT cm.user.username FROM ConversationMember cm
    WHERE cm.conversation.id = :conversationId
""")
    List<String> findConversationMembersByConversationId(Long conversationId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE ConversationMember cm
        SET cm.lastSeenMessageId = :messageId
        WHERE cm.conversation.id = :conversationId
        AND cm.user.id = :userId
    """)
    void updateLastSeenMessage(
            @Param("userId") Long userId,
            @Param("conversationId") Long conversationId,
            @Param("messageId") Long messageId
    );

    @Query("""
    SELECT COUNT(cm) > 0 FROM ConversationMember cm
    WHERE cm.conversation.id = :conversationId AND cm.user.id = :userId
""")
    boolean existsByConversationIdAndUserId(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
