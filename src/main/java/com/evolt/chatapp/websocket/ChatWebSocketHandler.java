package com.evolt.chatapp.websocket;

import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.dto.MessageDTO;
import com.evolt.chatapp.models.dto.UserDTO;
import com.evolt.chatapp.services.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(MessageService messageService, ObjectMapper objectMapper) {
        this.messageService = messageService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        String username = (String) session.getAttributes().get("username");

        if (username != null) {
            userSessions.put(username, session);

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(username);

            notifyUserJoin(userDTO);
        }
    }

    public void notifyUserJoin(UserDTO userDTO) {
        try {
            String message = objectMapper.writeValueAsString(
                    new UserPayload(userDTO)
            );

            broadcast(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        String username = (String) session.getAttributes().get("username");

        if (username != null) {
            userSessions.remove(username);

            try {
                String message = objectMapper.writeValueAsString(
                        new UserLeavePayload(username)
                );

                broadcast(message);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class UserLeavePayload {
        public String type = "user_leave";
        public String username;

        public UserLeavePayload(String username) {
            this.username = username;
        }
    }

    // Handler from FE request
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        var node = objectMapper.readTree(message.getPayload());
        String type = node.get("type").asText();

        switch (type) {
            case "message": {
                String sender = node.get("sender").asText();
                String receiver = node.hasNonNull("receiver")
                        ? node.get("receiver").asText()
                        : null;
                String content = node.get("content").asText();

                MessageDTO saved = messageService.saveMessageDTO(sender, receiver, content, null);
                notifyNewMessage(saved);
                break;
            }

            case "user": {
                broadcast(message.getPayload());
                break;
            }
        }

    }

    public void notifyNewUser(UserDTO userDTO) {
        try {
            String message = objectMapper.writeValueAsString(new UserPayload(userDTO));
            broadcast(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notifyNewMessage(MessageDTO messageDTO) {
        try {
            String message = objectMapper.writeValueAsString(
                    new MessagePayload(
                            messageDTO.getSender(),
                            messageDTO.getReceiver(),
                            messageDTO.getContent(),
                            messageDTO.getTimestamp()
                    )
            );

            if (messageDTO.getReceiver() == null) {
                broadcast(message);
            } else {
                sendToReceiver(messageDTO.getReceiver().getUsername(), message);
                sendToReceiver(messageDTO.getSender().getUsername(), message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notifyNewChatRequest(String sender, String receiver) {
        try {
            String message = objectMapper.writeValueAsString(
                    new PrivateRequestPayload(sender, receiver)
            );

            sendToReceiver(receiver, message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToReceiver(String username, String message) {

        WebSocketSession session = userSessions.get(username);

        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcast(String message) {

        userSessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static class UserPayload {
        public String type = "user";
        public UserDTO user;

        public UserPayload(UserDTO user) {
            this.user = user;
        }
    }

    private static class MessagePayload {
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

    private static class PrivateRequestPayload {
        public String type = "chatRequest";
        public String sender;
        public String receiver;

        public PrivateRequestPayload(String sender, String receiver) {
            this.sender = sender;
            this.receiver = receiver;
        }
    }
}