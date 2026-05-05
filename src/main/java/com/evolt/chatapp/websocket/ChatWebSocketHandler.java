package com.evolt.chatapp.websocket;

import com.evolt.chatapp.models.dto.MessageDTO;
import com.evolt.chatapp.models.dto.UserDTO;
import com.evolt.chatapp.services.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MessageService messageService;

    public ChatWebSocketHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {


        String username = (String) session.getAttributes().get("username");

        if (username != null) {
            userSessions.put(username, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,
                                      org.springframework.web.socket.CloseStatus status) {

        String username = (String) session.getAttributes().get("username");

        if (username != null) {
            userSessions.remove(username);
        }
    }
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        var node = objectMapper.readTree(message.getPayload());
        String type = node.get("type").asText();

        switch (type) {

            case "chat": {

                String sender = node.get("sender").asText();
                String receiver = node.hasNonNull("receiver")
                        ? node.get("receiver").asText()
                        : null;

                String content = node.get("content").asText();

                messageService.saveMessage(sender, receiver, content, null);

                broadcast(message.getPayload());
                break;
            }
            case "private": {

                String sender = node.get("sender").asText();
                String receiver = node.hasNonNull("receiver")
                        ? node.get("receiver").asText()
                        : null;

                String content = node.get("content").asText();

                messageService.saveMessage(sender, receiver, content, null);

                sendToReceiver(sender, message.getPayload());
                sendToReceiver(receiver, message.getPayload());
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
            String message;

            if (messageDTO.getReceiver() == null) {
                message = objectMapper.writeValueAsString(new ChatPayload(
                        messageDTO.getSender(),
                        messageDTO.getContent(),
                        messageDTO.getTimestamp()
                ));
                broadcast(message);

            } else {
                message = objectMapper.writeValueAsString(new PrivatePayload(
                        messageDTO.getSender(),
                        messageDTO.getReceiver(),
                        messageDTO.getContent(),
                        messageDTO.getTimestamp()
                ));

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

    private static class PrivatePayload {
        public String type = "private";
        public UserDTO sender;
        public UserDTO receiver;
        public String content;
        public String timestamp;

        public PrivatePayload(UserDTO sender, UserDTO receiver, String content, String timestamp) {
            this.sender = sender;
            this.receiver = receiver;
            this.content = content;
            this.timestamp = timestamp;
        }
    }

    private static class UserPayload {
        public String type = "user";
        public UserDTO user;

        public UserPayload(UserDTO user) {
            this.user = user;
        }
    }

    private static class ChatPayload {
        public String type = "chat";
        public UserDTO sender;
        public String content;
        public String timestamp;

        public ChatPayload(UserDTO sender, String content, String timestamp) {
            this.sender = sender;
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