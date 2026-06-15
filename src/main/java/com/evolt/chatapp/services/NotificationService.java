package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Notification;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.UserDto;
import com.evolt.chatapp.models.enums.NotificationStatus;
import com.evolt.chatapp.models.enums.NotificationType;
import com.evolt.chatapp.events.NewUserNotificationEvent;
import com.evolt.chatapp.repositories.NotificationRepository;
import com.evolt.chatapp.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher; // Changed here

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               ApplicationEventPublisher eventPublisher) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void createNewUserNotifications(UserDto userDto) {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (user.getId().equals(userDto.getId())) continue;

            Notification notification = new Notification();



            notification.setRecipient(user);

            notification.setType(NotificationType.USER_JOINED);

            notification.setReferenceId(userDto.getId());

            notification.setContent(userDto.getUsername() + " is online");

            notification.setStatus(NotificationStatus.PENDING);

            notification.setTimestamp(LocalDateTime.now());

            notificationRepository.save(notification);

            eventPublisher.publishEvent(new NewUserNotificationEvent(notification));
        }
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
    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsDelivered(Long id) {
        notificationRepository.markAllAsDelivered(id);
    }

    @Transactional
    public void setStatus(Long id, String status) {
        Optional<Notification> notification = notificationRepository.findById(id);
        if (notification.isPresent()) {
            notification.get().setStatus(NotificationStatus.valueOf(status));
            notificationRepository.save(notification.get());
        }
        else {
            // BI
        }
    }

}
