package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("""
    SELECT DISTINCT c FROM Conversation c
    WHERE c.type = "GLOBAL"
""")
    Conversation findGlobalConversation();
}
