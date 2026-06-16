package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Notification;
import com.evolt.chatapp.models.dto.NotificationDto;
import com.evolt.chatapp.models.enums.NotificationStatus;
import com.evolt.chatapp.models.mappers.NotificationMapper;
import com.evolt.chatapp.services.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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

    @PatchMapping("/{id}/status")
    public ResponseEntity<NotificationDto> updateNotificationStatus(
            @PathVariable Long id,
            @RequestBody String status) {

        Notification notification = notificationService.findById(id);

        if (notification == null) {
            return ResponseEntity.notFound().build();
        }

        System.out.println("update statusa");
        notification.setStatus(NotificationStatus.valueOf(status));
        notificationService.save(notification);
        System.out.println(notification.getStatus());
        return ResponseEntity.ok(NotificationMapper.toDTO(notification));
    }
}
