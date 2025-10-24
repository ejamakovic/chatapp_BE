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
    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson mapper

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("Novi korisnik povezan: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("Korisnik se odjavio: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Prosledi poruku svima
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(message);
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
            String message = objectMapper.writeValueAsString(new ChatPayload(
                    messageDTO.getSender(),
                    messageDTO.getContent(),
                    messageDTO.getTimestamp()
            ));
            broadcast(message);
        } catch (Exception e) {
            e.printStackTrace();
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
}
