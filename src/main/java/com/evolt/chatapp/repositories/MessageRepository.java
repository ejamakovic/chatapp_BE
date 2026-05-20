package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @EntityGraph(attributePaths = "attachments")
    @Query("""
    SELECT m from Message m
    WHERE m.conversation.id = :id
    ORDER BY m.timestamp DESC
""")
    Page<Message> findMessagesFromConversation(Long id, Pageable pageable);

}

