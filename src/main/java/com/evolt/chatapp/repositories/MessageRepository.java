package com.evolt.chatapp.repositories;


import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderAndReceiverOrReceiverAndSenderOrderByTimestampAsc(User sender, User receiver, User sender1, User receiver1);

    List<Message> findByReceiverOrderByTimestampAsc(User receiver);
}

