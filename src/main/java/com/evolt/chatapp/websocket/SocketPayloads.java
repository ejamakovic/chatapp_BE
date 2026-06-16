package com.evolt.chatapp.websocket;

import com.evolt.chatapp.models.Notification;
import com.evolt.chatapp.models.dto.AttachmentDto;
import com.evolt.chatapp.models.dto.MessageDto;
import com.evolt.chatapp.models.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

public class SocketPayloads {

    // MESSAGE EVENT RECORD
    public record MessagePayload(
            String type,
            Long id,
            Long conversationId,
            UserDto sender,
            String content,
            LocalDateTime timestamp,
            List<AttachmentDto> attachments
    ) {
        public MessagePayload(MessageDto dto) {
            this(
                    "message",
                    dto.getId(),
                    dto.getConversationId(),
                    dto.getSender(),
                    dto.getContent(),
                    dto.getTimestamp(),
                    dto.getAttachments()
            );
        }
    }

    // NOTIFICATION EVENT RECORD
    public record NotificationPayload(
            String type,
            Long id,
            UserDto recipient,
            Long referenceId,
            String notificationType,
            String status,
            String content,
            LocalDateTime timestamp
    ) {
        public NotificationPayload(Notification notification) {
            this(
                    "notification",
                    notification.getId(),
                    notification.getRecipient() != null ? new UserDto(notification.getRecipient()) : null,
                    notification.getReferenceId(),
                    String.valueOf(notification.getType()),
                    String.valueOf(notification.getStatus()),
                    notification.getContent(),
                    notification.getTimestamp()
            );
        }
    }

    // TYPING EVENT RECORD
    public record TypingPayload(
            String type,
            Long conversationId,
            UserDto user,
            boolean typing
    ) {
        public TypingPayload(Long conversationId, UserDto user, boolean typing) {
            this("typing", conversationId, user, typing);
        }
    }
}