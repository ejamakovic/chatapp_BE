package com.evolt.chatapp.services;

import com.evolt.chatapp.repositories.ConversationMemberRepository;
import org.springframework.stereotype.Service;

@Service
public class ConversationMemberService {

    private final ConversationMemberRepository conversationMemberRepository;

    public ConversationMemberService(ConversationMemberRepository conversationMemberRepository) {
        this.conversationMemberRepository = conversationMemberRepository;
    }


}
