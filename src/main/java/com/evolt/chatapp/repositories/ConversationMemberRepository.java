package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {


    @Query("""
    SELECT DISTINCT cm.user.username FROM ConversationMember cm
    WHERE cm.conversation.id = :conversationId
""")
    List<String> findConversationMembersByConversationId(Long conversationId);

    @Modifying
    @Query("""
UPDATE ConversationMember cm
SET cm.lastSeenMessageId = :messageId
WHERE cm.conversation.id = :conversationId
AND cm.user.id = :userId
""")
    void updateLastSeenMessage(
            Long conversationId,
            Long userId,
            Long messageId
    );
}
