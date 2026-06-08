package com.evolt.chatapp.services;

import com.evolt.chatapp.repositories.FriendshipRepository;
import org.springframework.stereotype.Service;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;

    public FriendshipService(FriendshipRepository friendshipRepository) {
        this.friendshipRepository = friendshipRepository;
    }

}
