package com.evolt.chatapp.websocket;

import com.evolt.chatapp.models.dto.UserDTO;

public class SocketPayloads {

    public static class UserPayload {
        public String type = "user";
        public UserDTO user;

        public UserPayload(UserDTO user) {
            this.user = user;
        }
    }

    public static class MessagePayload {
        public String type = "message";
        public UserDTO sender;
        public UserDTO receiver;
        public String content;
        public String timestamp;

        public MessagePayload(UserDTO sender, UserDTO receiver, String content, String timestamp) {
            this.sender = sender;
            this.receiver = receiver;
            this.content = content;
            this.timestamp = timestamp;
        }
    }

    public static class PrivateRequestPayload {
        public String type = "chatRequest";
        public String sender;
        public String receiver;

        public PrivateRequestPayload(String sender, String receiver) {
            this.sender = sender;
            this.receiver = receiver;
        }
    }

    public static class UserLeavePayload {
        public String type = "user_leave";
        public String username;

        public UserLeavePayload(String username) {
            this.username = username;
        }
    }
}
