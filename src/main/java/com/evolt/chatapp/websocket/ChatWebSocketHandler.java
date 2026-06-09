package com.evolt.chatapp.websocket;

import com.evolt.chatapp.models.Notification;
import com.evolt.chatapp.models.dto.MessageDto;
import com.evolt.chatapp.models.dto.UserDto;
import com.evolt.chatapp.services.ConversationMemberService;
import com.evolt.chatapp.services.NotificationService;
import com.evolt.chatapp.services.UserService;
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

    private final UserService userService;

    private static final Logger logger =
            LoggerFactory.getLogger(ChatWebSocketHandler.class);
    
    private final ConversationMemberService conversationMemberService;
    private final NotificationService notificationService;

    public ChatWebSocketHandler(ObjectMapper objectMapper, ConversationMemberService conversationMemberService, UserService userService, NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.conversationMemberService = conversationMemberService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    // SEND TO USER
    private void sendToUser(String username, Object payload) {
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
        Long id = (Long) session.getAttributes().get("id");

        if (username == null || id == null) {
            return;
        }

        userSessions.put(username, session);

        userService.setConnected(username, true);
        logger.info("User connected: {}", username);

        List<Notification> notifications =
                notificationService.findAllPendingFromUser(id);

        notifications.forEach(notification -> {
            sendToUser(
                    username,
                    new SocketPayloads.NotificationPayload(notification)
            );
        });

        notificationService.markAllAsDelivered(id);

        notificationService.createUserOnlineNotifications(id, username);

        notifyUserOnline(new UserDto(id, username));
    }

    // DISCONNECT
    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ) {

        String username =
                (String) session.getAttributes().get("username");

        if (username == null) return;

        userSessions.remove(username);

        userService.setConnected(username, false);
        logger.info("User disconnected: {}", username);
    }

    // BROADCAST ALL USERS
    private void sendToAll(Object payload) {
        userSessions.keySet().forEach(user -> sendToUser(user, payload));
    }

    // USER ONLINE EVENT
    public void notifyUserOnline(UserDto userDTO) {
        sendToAll(new SocketPayloads.UserPayload(userDTO));
    }

    // NEW MESSAGE
    public void notifyNewMessage(MessageDto messageDTO) {

        try {

            SocketPayloads.MessagePayload payload = new SocketPayloads.MessagePayload(messageDTO);

            List<String> participants =
                    conversationMemberService.getParticipants(
                            messageDTO.getConversationId()
                    );

            for (String username : participants) {

                if (userSessions.containsKey(username)) {
                    sendToUser(username, payload);
                }
            }

        } catch (Exception e) {

            logger.error(
                    "WS message error conversation={}",
                    messageDTO.getConversationId(),
                    e
            );
        }
    }

}