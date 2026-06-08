package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Notification;
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
    public ResponseEntity<Notification> getNotification(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.findById(id));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<Notification>> getNotificationByUserId(@PathVariable Long id) {
        return ResponseEntity.of(Optional.ofNullable(notificationService.findAllFromUser(id)));
    }
}
