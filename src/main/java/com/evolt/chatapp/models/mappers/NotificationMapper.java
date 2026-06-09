package com.evolt.chatapp.models.mappers;

import com.evolt.chatapp.models.Notification;
import com.evolt.chatapp.models.dto.NotificationDto;

public class NotificationMapper {

    public static NotificationDto toDTO(Notification notification) {
        if (notification == null) return null;

        return new NotificationDto(notification);
    }
}