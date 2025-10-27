package com.evolt.chatapp.websocket;

import com.evolt.chatapp.models.dto.MessageDTO;
import com.evolt.chatapp.models.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("Novi korisnik povezan: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("Korisnik se odjavio: " + session.getAttributes().get("username"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        var node = objectMapper.readTree(message.getPayload());
        String type = node.get("type").asText();

        switch (type) {
            case "init":
                String username = node.get("username").asText();
                session.getAttributes().put("username", username);
                System.out.println("Korisnik registrovan u session: " + username);
                break;

            case "user":
                broadcast(message.getPayload());
                break;

            case "chat":
                broadcast(message.getPayload());
                break;

            case "private":
                String toUsername = node.get("to").asText();
                sessions.stream()
                        .filter(s -> toUsername.equals(s.getAttributes().get("username")))
                        .findFirst()
                        .ifPresent(receiver -> {
                            try {
                                receiver.sendMessage(new TextMessage(message.getPayload()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                break;

            default:
                System.out.println("Nepoznat tip poruke: " + type);
                break;
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToReceiver(String receiverUsername, String message) {
        sessions.forEach(session -> {
            String sessionUsername = (String) session.getAttributes().get("username");
            if (sessionUsername != null && sessionUsername.equals(receiverUsername) && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void notifyNewChatRequest(String sender, String receiver) {
        try {
            String message = objectMapper.writeValueAsString(new PrivateRequestPayload(
                    sender,
                    receiver
            ));
            sendToReceiver(receiver, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void broadcast(String message) {
        sessions.forEach(s -> {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage(message));
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
