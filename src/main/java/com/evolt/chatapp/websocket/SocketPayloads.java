package com.evolt.chatapp.websocket;

import com.evolt.chatapp.models.Notification;
import com.evolt.chatapp.models.dto.AttachmentDto;
import com.evolt.chatapp.models.dto.MessageDto;
import com.evolt.chatapp.models.dto.UserDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SocketPayloads {

    // USER EVENTS
    public static class UserPayload {

        public String type = "user_join";
        public UserDto user;

        public UserPayload(String type, UserDto user) {
            this.type = type;
            this.user = user;
        }

        public UserPayload(UserDto user) {
            this.user = user;
        }
    }

    // MESSAGE EVENT
    public static class MessagePayload {

        public String type = "message";

        public Long id;

        public Long conversationId;

        public UserDto sender;

        public String content;

        public LocalDateTime timestamp;

        public List<AttachmentDto> attachments = new ArrayList<>();

        public MessagePayload() {}

        public MessagePayload(
                MessageDto messageDTO
        ) {
            this.id = messageDTO.getId();
            this.conversationId = messageDTO.getConversationId();
            this.sender = messageDTO.getSender();
            this.content = messageDTO.getContent();
            this.timestamp = messageDTO.getTimestamp();
            this.attachments = messageDTO.getAttachments();
        }
    }

    // NOTIFICATION EVENT
    public static class NotificationPayload {

        public String type = "notification";

        public Long id;

        public UserDto recipient;

        public Long referenceId;

        public String notificationType;

        public String status;

        public String content;

        public LocalDateTime timestamp;

        public NotificationPayload(Notification notification) {
            this.id = notification.getId();
            this.recipient = notification.getRecipient();
            this.notificationType = String.valueOf(notification.getType());
            this.status = String.valueOf(notification.getStatus());
            this.content = String.valueOf(notification.getContent());
            this.referenceId = notification.getReferenceId();
            this.timestamp = notification.getTimestamp();
        }
    }


    // TYPING EVENT
    public static class TypingPayload {

        public String type = "typing";

        public Long conversationId;

        public UserDto user;

        public boolean typing;

        public TypingPayload(Long conversationId, UserDto user, boolean typing) {
            this.conversationId = conversationId;
            this.user = user;
            this.typing = typing;
        }
    }
}