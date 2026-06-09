package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Notification;
import com.evolt.chatapp.models.dto.NotificationDto;
import com.evolt.chatapp.models.mappers.NotificationMapper;
import com.evolt.chatapp.services.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDto> getNotification(@PathVariable Long id) {
        return ResponseEntity.ok(
                NotificationMapper.toDTO(
                        notificationService.findById(id)
                )
        );
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<NotificationDto>> getNotificationByUserId(
            @PathVariable Long id) {

        List<NotificationDto> notifications =
                notificationService.findAllFromUser(id)
                        .stream()
                        .map(NotificationMapper::toDTO)
                        .toList();

        return ResponseEntity.ok(notifications);
    }
}
