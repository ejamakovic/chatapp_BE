package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Friendship;
import com.evolt.chatapp.models.Notification;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.enums.FriendshipStatus;
import com.evolt.chatapp.models.enums.NotificationStatus;
import com.evolt.chatapp.models.enums.NotificationType;
import com.evolt.chatapp.repositories.FriendshipRepository;
import com.evolt.chatapp.repositories.NotificationRepository;
import com.evolt.chatapp.repositories.UserRepository;
import com.evolt.chatapp.websocket.WebSocketEvent;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public FriendshipService(
            FriendshipRepository friendshipRepository,
            UserRepository userRepository,
            NotificationRepository notificationRepository,
            ApplicationEventPublisher eventPublisher)
    {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Friendship save(Friendship friendship) {
        return friendshipRepository.save(friendship);
    }

    public Friendship findById(Long id) {
        return friendshipRepository.findById(id).orElse(null);
    }

    @Transactional
    public void sendRequest(Long requesterId, Long addresseeId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("Requester not found with ID: " + requesterId));

        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new IllegalArgumentException("Addressee not found with ID: " + addresseeId));

        Friendship friendship = new Friendship(requester, addressee, FriendshipStatus.PENDING);
        friendship.setCreatedAt(LocalDateTime.now());

        friendshipRepository.save(friendship);
        Notification notification = new Notification(
                addressee,
                NotificationType.FRIEND_REQUEST,
                friendship.getId(),
                "New friend request from " + requester.getUsername(),
                NotificationStatus.PENDING,
                LocalDateTime.now()
        );
        notificationRepository.save(notification);
        eventPublisher.publishEvent(new WebSocketEvent<>("NOTIFICATION", notification));
    }

    @Transactional
    public void updateFriendship(Long id, String status) {
        Friendship friendship = friendshipRepository.getReferenceById(id);

        friendship.setStatus(FriendshipStatus.valueOf(status));

        notificationRepository.closeFriendRequestNotification(id);
    }

}