package com.evolt.chatapp.websocket;

import com.evolt.chatapp.events.WebSocketEvent;
import com.evolt.chatapp.models.Notification;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.MessageDto;
import com.evolt.chatapp.models.enums.NotificationStatus;
import com.evolt.chatapp.services.ConversationMemberService;
import com.evolt.chatapp.services.NotificationService;
import com.evolt.chatapp.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
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

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final ConversationMemberService conversationMemberService;
    private final NotificationService notificationService;
    private final UserService userService;

    public ChatWebSocketHandler(ObjectMapper objectMapper,
                                ConversationMemberService conversationMemberService,
                                NotificationService notificationService, UserService userService) {
        this.objectMapper = objectMapper;
        this.conversationMemberService = conversationMemberService;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    // --- CENTRALIZED EVENT ROUTER ---
    @Async
    @EventListener
    public void handleWebSocketEvents(WebSocketEvent<?> event) {
        switch (event.getEventType()) {
            case "NEW_MESSAGE" -> {
                MessageDto messageDto = (MessageDto) event.getPayload();
                SocketPayloads.MessagePayload payload = new SocketPayloads.MessagePayload(messageDto);

                List<String> participants = conversationMemberService.getParticipants(messageDto.getConversationId());
                for (String username : participants) {
                    boolean delivered = sendToUser(username, payload);
                }
            }
            case "USER_JOINED" -> {
                // Need to add payload if I want this
            }
            case "NOTIFICATION" -> {
                Notification notification = (Notification) event.getPayload();

                boolean delivered = sendToUser(
                        notification.getRecipient().getUsername(),
                        new SocketPayloads.NotificationPayload(notification)
                );

                if (delivered) {
                    notification.setStatus(NotificationStatus.DELIVERED);
                    notificationService.save(notification);
                }
            }
            default -> logger.warn("Received unhandled WebSocket event type: {}", event.getEventType());
        }
    }


    // --- CONNECTION HANDLERS ---
    @Override
    @Transactional
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("afterConnectionEstablished called");
        String username = (String) session.getAttributes().get("username");
        Long id = (Long) session.getAttributes().get("id");

        if (username == null || id == null) return;

        userSessions.put(username, session);
        logger.info("User connected: {}", username);

        userService.setConnected(id, true);

        // Fetch and flush missed offline notifications
        List<Notification> notifications = notificationService.findAllPendingFromUser(id);
        for (Notification notification : notifications) {
            sendToUser(username, new SocketPayloads.NotificationPayload(notification));
        }
        notificationService.markAllAsDelivered(id);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("afterConnectionClosed called");
        String username = (String) session.getAttributes().get("username");
        Long id = (Long) session.getAttributes().get("id");
        if (username != null) {
            userSessions.remove(username);
            logger.info("User disconnected: {}", username);
            userService.setConnected(id, false);
        }
    }

    // --- SEND UTILITIES ---
    private boolean sendToUser(String username, Object payload) {
        WebSocketSession session = userSessions.get(username);

        if (session == null || !session.isOpen()) {
            return false;
        }

        try {
            String json = objectMapper.writeValueAsString(payload);
            session.sendMessage(new TextMessage(json));
            return true;
        } catch (IOException e) {
            logger.error("Failed to send WS message to {}", username, e);
            return false;
        }
    }

    private void sendToAll(Object payload) {
        userSessions.keySet().forEach(user -> sendToUser(user, payload));
    }
}