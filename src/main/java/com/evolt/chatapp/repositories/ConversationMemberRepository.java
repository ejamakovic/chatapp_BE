package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {


    @Query("""
    SELECT DISTINCT cm.user.username FROM ConversationMember cm
    WHERE cm.conversation.id = :conversationId
""")
    List<String> findConversationMembersByConversationId(Long conversationId);
}
