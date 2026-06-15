package com.evolt.chatapp.events;

import com.evolt.chatapp.models.Notification;

public class NewUserNotificationEvent {
    private final Notification notification;

    public NewUserNotificationEvent(Notification notification) {
        this.notification = notification;
    }

    public Notification getNotification() {
        return notification;
    }
}