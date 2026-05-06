package com.evolt.chatapp.repositories;


import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderAndReceiverOrReceiverAndSenderOrderByTimestampAsc(User sender, User receiver, User sender1, User receiver1);

    List<Message> findByReceiverOrderByTimestampAsc(User receiver);

    @Query("""
    SELECT m FROM Message m
    WHERE m.receiver IS null
    ORDER BY m.timestamp DESC
""")
    Page<Message> findByReceiverIsNull(Pageable pageable);

    @Query("""
    SELECT m FROM Message m
    WHERE 
        (m.sender = :user1 AND m.receiver = :user2)
        OR
        (m.sender = :user2 AND m.receiver = :user1)
    ORDER BY m.timestamp DESC
""")
    Page<Message> findPrivateChat(User user1, User user2, Pageable pageable);

    @Query("""
SELECT m FROM Message m
WHERE (m.sender = :user OR m.receiver = :user)
AND m.timestamp = (
    SELECT MAX(m2.timestamp)
    FROM Message m2
    WHERE 
        (
            m2.sender = m.sender AND m2.receiver = m.receiver
        )
        OR
        (
            m2.sender = m.receiver AND m2.receiver = m.sender
        )
)
ORDER BY m.timestamp DESC
""")
    Page<Message> findAllPrivateChatsFromUser(User user, Pageable pageable);
}

