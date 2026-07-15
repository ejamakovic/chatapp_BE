package com.evolt.chatapp.websocket;

import com.evolt.chatapp.models.Notification;
import com.evolt.chatapp.models.dto.AttachmentDto;
import com.evolt.chatapp.models.dto.MessageDto;
import com.evolt.chatapp.models.dto.MessageReactionDto;
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

    // REACTION EVENT RECORD
    public record ReactionPayload(
            String type,
            Long id,
            Long messageId,
            Long conversationId,
            UserDto user,
            String emoji,
            LocalDateTime timestamp
    ) {
        public ReactionPayload(MessageReactionDto dto) {
            this(
                    "reaction_added",
                    dto.getId(),
                    dto.getMessageId(),
                    dto.getConversationId(),
                    dto.getUser(),
                    dto.getEmoji(),
                    dto.getTimestamp()
            );
        }
    }

}