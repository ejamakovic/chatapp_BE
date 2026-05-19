package com.evolt.chatapp.websocket;

import com.evolt.chatapp.models.dto.MessageDTO;
import com.evolt.chatapp.models.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    private static final Logger logger =
            LoggerFactory.getLogger(ChatWebSocketHandler.class);

    public ChatWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // SEND RAW
    private void sendRaw(String username, Object payload) {
        WebSocketSession session = userSessions.get(username);

        if (session == null || !session.isOpen()) return;

        try {
            session.sendMessage(
                    new TextMessage(objectMapper.writeValueAsString(payload))
            );
        } catch (IOException e) {
            logger.error("Failed to send WS message to {}", username, e);
        }
    }

    // CONNECT
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        String username = (String) session.getAttributes().get("username");

        if (username == null) return;

        userSessions.put(username, session);

        logger.info("User connected: {}", username);

        notifyUserOnline(new UserDTO(username));
    }

    // DISCONNECT
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        String username = (String) session.getAttributes().get("username");

        if (username == null) return;

        userSessions.remove(username);

        logger.info("User disconnected: {}", username);
    }

    // BROADCAST ALL USERS
    private void sendToAll(Object payload) {
        userSessions.keySet().forEach(user -> sendRaw(user, payload));
    }

    // USER ONLINE EVEN
    public void notifyUserOnline(UserDTO userDTO) {
        sendToAll(new SocketPayloads.UserPayload("ONLINE", userDTO));
    }

    // NEW MESSAGE
    public void notifyNewMessage(MessageDTO messageDTO) {

        System.out.println("Message id " + messageDTO.getAttachments());
        try {
            List<String> attachments =
                    messageDTO.getAttachments() == null
                            ? List.of()
                            : messageDTO.getAttachments()
                            .stream()
                            .map(a -> a.getFileUrl())
                            .toList();

            SocketPayloads.MessagePayload payload =
                    new SocketPayloads.MessagePayload(
                            messageDTO.getId(),
                            messageDTO.getConversationId(),
                            messageDTO.getSender(),
                            messageDTO.getContent(),
                            messageDTO.getTimestamp(),
                            attachments
                    );

            // SEND TO EVERY CONNECTED USER
            sendToAll(payload);

        } catch (Exception e) {
            logger.error(
                    "WS message error sender={}, conversation={}",
                    messageDTO.getSender(),
                    messageDTO.getConversationId(),
                    e
            );
        }
    }
}