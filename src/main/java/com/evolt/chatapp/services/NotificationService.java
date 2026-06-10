package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Notification;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.enums.NotificationStatus;
import com.evolt.chatapp.models.enums.NotificationType;
import com.evolt.chatapp.repositories.NotificationRepository;
import com.evolt.chatapp.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public Notification findById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }

    public List<Notification> findAllFromUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    public List<Notification> findAllPendingFromUser(Long userId) {
        return notificationRepository.findPendingFromUserId(userId);
    }

    @Transactional
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsDelivered(Long id) {
        notificationRepository.markAllAsDelivered(id);
    }

    @Transactional
    public void createNewUserNotifications(Long userId, String username) {

        List<User> users = userRepository.findAll();

        for (User user : users) {

            if (user.getId().equals(userId)) {
                continue;
            }

            Notification notification = new Notification();

            notification.setRecipient(user);
            notification.setType(NotificationType.USER_JOINED);
            notification.setReferenceId(userId);
            notification.setContent(username + " is online");
            notification.setStatus(NotificationStatus.PENDING);
            notification.setTimestamp(LocalDateTime.now());

            notificationRepository.save(notification);

        }
    }

}
