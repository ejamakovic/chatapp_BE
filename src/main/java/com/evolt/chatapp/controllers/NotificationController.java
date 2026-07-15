package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Notification;
import com.evolt.chatapp.models.dto.NotificationDto;
import com.evolt.chatapp.models.enums.NotificationStatus;
import com.evolt.chatapp.models.mappers.NotificationMapper;
import com.evolt.chatapp.services.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<?> getNotification(@PathVariable Long id, HttpServletRequest request) {
        Long requesterId = Long.parseLong(request.getAttribute("userId").toString());
        Notification notification = notificationService.findById(id);

        if (notification == null) return ResponseEntity.notFound().build();
        if (!notification.getRecipient().getId().equals(requesterId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(NotificationMapper.toDTO(notification));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getNotificationByUserId(@PathVariable Long id, HttpServletRequest request) {
        Long requesterId = Long.parseLong(request.getAttribute("userId").toString());
        if (!id.equals(requesterId)) {
            return ResponseEntity.status(403).build();
        }
        List<NotificationDto> notifications = notificationService.findAllFromUser(id)
                .stream().map(NotificationMapper::toDTO).toList();
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateNotificationStatus(
            @PathVariable Long id,
            @RequestBody String status,
            HttpServletRequest request) {

        Long requesterId = Long.parseLong(request.getAttribute("userId").toString());
        Notification notification = notificationService.findById(id);

        if (notification == null) return ResponseEntity.notFound().build();
        if (!notification.getRecipient().getId().equals(requesterId)) {
            return ResponseEntity.status(403).build();
        }

        notification.setStatus(NotificationStatus.valueOf(status));
        notificationService.save(notification);
        return ResponseEntity.ok(NotificationMapper.toDTO(notification));
    }
}
