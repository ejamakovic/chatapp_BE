package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
}
