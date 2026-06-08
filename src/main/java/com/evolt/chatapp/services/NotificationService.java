package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Notification;
import com.evolt.chatapp.repositories.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification findById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }

    public List<Notification> findAllFromUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

}
