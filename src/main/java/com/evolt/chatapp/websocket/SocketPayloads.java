package com.evolt.chatapp.websocket;

import com.evolt.chatapp.models.Attachment;
import com.evolt.chatapp.models.dto.UserDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SocketPayloads {

    public static class UserPayload {
        public String type = "user_join";
        public UserDTO user;

        public UserPayload(UserDTO user) {
            this.user = user;
        }

        public UserPayload(String type, String username) {
            this.user = new UserDTO();
            this.user.setUsername(username);
            this.type = type;
        }
    }

    public static class MessagePayload {
        public String type = "message";
        public Long id;
        public UserDTO sender;
        public UserDTO receiver;
        public String content;
        public LocalDateTime timestamp;
        public List<Attachment> attachments = new ArrayList<>();

        public MessagePayload(Long id, UserDTO sender, UserDTO receiver, String content, LocalDateTime timestamp, List<Attachment> attachments) {
            this.id = id;
            this.sender = sender;
            this.receiver = receiver;
            this.content = content;
            this.timestamp = timestamp;
            this.attachments = attachments;
        }

        public MessagePayload(String type ,Long id, UserDTO sender, UserDTO receiver, String content, LocalDateTime timestamp, List<Attachment> attachments) {
            this.type = type;
            this.id = id;
            this.sender = sender;
            this.receiver = receiver;
            this.content = content;
            this.timestamp = timestamp;
            this.attachments = attachments;
        }
    }

    public static class PrivateRequestPayload {
        public String type = "chat_request";
        public String sender;
        public String receiver;

        public PrivateRequestPayload(String sender, String receiver) {
            this.sender = sender;
            this.receiver = receiver;
        }
    }

}
