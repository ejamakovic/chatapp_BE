package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.Conversation;
import com.evolt.chatapp.models.ConversationMember;
import com.evolt.chatapp.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {

    @Query("""
    SELECT DISTINCT cm.conversation
        FROM ConversationMember cm
        WHERE cm.user.id = :userId
        ORDER BY cm.conversation.createdAt DESC
""")
    Page<Conversation> findUserConversations(Long userId, Pageable pageable);

    @Query("""
    SELECT DISTINCT cm.user.username FROM ConversationMember cm
    WHERE cm.conversation.id = :conversationId
""")
    List<String> findConversationMembersByConversationId(Long conversationId);
}
