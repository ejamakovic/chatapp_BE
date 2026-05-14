package com.evolt.chatapp.websocket;

import com.evolt.chatapp.models.dto.UserDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SocketPayloads {

    // USER EVENTS
    public static class UserPayload {

        public String type; // user_join, user_leave
        public UserDTO user;

        public UserPayload(String type, UserDTO user) {
            this.type = type;
            this.user = user;
        }

        public UserPayload(UserDTO user) {
            this.type = "user_join";
            this.user = user;
        }
    }

    // MESSAGE EVENT (NEW MODEL)
    public static class MessagePayload {

        public String type = "message";

        public Long messageId;

        public Long conversationId;

        public UserDTO sender;

        public String content;

        public LocalDateTime timestamp;

        public List<String> attachmentUrls = new ArrayList<>();

        public MessagePayload() {}

        public MessagePayload(
                Long messageId,
                Long conversationId,
                UserDTO sender,
                String content,
                LocalDateTime timestamp,
                List<String> attachmentUrls
        ) {
            this.messageId = messageId;
            this.conversationId = conversationId;
            this.sender = sender;
            this.content = content;
            this.timestamp = timestamp;
            this.attachmentUrls = attachmentUrls;
        }

        public MessagePayload(
                String type,
                Long messageId,
                Long conversationId,
                UserDTO sender,
                String content,
                LocalDateTime timestamp,
                List<String> attachmentUrls
        ) {
            this.type = type;
            this.messageId = messageId;
            this.conversationId = conversationId;
            this.sender = sender;
            this.content = content;
            this.timestamp = timestamp;
            this.attachmentUrls = attachmentUrls;
        }
    }

    // NOTIFICATION EVENT
    public static class NotificationPayload {

        public String type = "notification";

        public Long conversationId;

        public String title;

        public String preview;

        public Long messageId;

        public Long unreadCount;

        public UserDTO sender;

        public NotificationPayload() {}

        public NotificationPayload(
                Long conversationId,
                String title,
                String preview,
                Long messageId,
                Long unreadCount,
                UserDTO sender
        ) {
            this.conversationId = conversationId;
            this.title = title;
            this.preview = preview;
            this.messageId = messageId;
            this.unreadCount = unreadCount;
            this.sender = sender;
        }
    }


    // TYPING EVENT
    public static class TypingPayload {

        public String type = "typing";

        public Long conversationId;

        public UserDTO user;

        public boolean typing;

        public TypingPayload(Long conversationId, UserDTO user, boolean typing) {
            this.conversationId = conversationId;
            this.user = user;
            this.typing = typing;
        }
    }
}