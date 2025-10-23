package com.evolt.chatapp.repositories;


import com.evolt.chatapp.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

}

