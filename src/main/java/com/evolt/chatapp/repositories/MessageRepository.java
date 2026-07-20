package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
    SELECT m from Message m
    WHERE m.conversation.id = :id
    ORDER BY m.timestamp DESC
""")
    Page<Message> findMessagesFromConversation(Long id, Pageable pageable);

    @Query("""
    SELECT m FROM Message m
    WHERE m.conversation.id = :conversationId AND m.id <= :messageId
    ORDER BY m.timestamp DESC
""")
    Page<Message> findMessagesUpToId(@Param("conversationId") Long conversationId,
                                     @Param("messageId") Long messageId,
                                     Pageable pageable);

    @Query("""
    SELECT m FROM Message m
    WHERE m.conversation.id = :conversationId AND m.id > :messageId
    ORDER BY m.timestamp ASC
""")
    Page<Message> findMessagesAfterId(@Param("conversationId") Long conversationId,
                                      @Param("messageId") Long messageId,
                                      Pageable pageable);

    @Query("""
    SELECT m FROM Message m
    WHERE m.conversation.id = :conversationId AND m.id < :messageId
    ORDER BY m.timestamp DESC
""")
    Page<Message> findMessagesBeforeId(@Param("conversationId") Long conversationId,
                                       @Param("messageId") Long messageId,
                                       Pageable pageable);

}

