package com.evolt.chatapp.websocket;

import com.evolt.chatapp.models.dto.MessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // username -> session
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    // conversationId -> users currently viewing chat
    private final Map<Long, Set<String>> activeConversations = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    private void sendRaw(String username, String json) {

        WebSocketSession session = userSessions.get(username);

        if (session != null && session.isOpen()) {

            try {
                session.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ChatWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // CONNECT
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        String username = (String) session.getAttributes().get("username");

        if (username != null) {
            userSessions.put(username, session);
        }
    }


    // DISCONNECT
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        String username = (String) session.getAttributes().get("username");

        if (username != null) {

            userSessions.remove(username);

            // ukloni iz svih active conversations
            activeConversations.values().forEach(set -> set.remove(username));
        }
    }

    // JOIN CHAT ROOM
    public void joinConversation(Long conversationId, String username) {

        activeConversations
                .computeIfAbsent(conversationId, k -> ConcurrentHashMap.newKeySet())
                .add(username);
    }

    // LEAVE CHAT ROOM
    public void leaveConversation(Long conversationId, String username) {

        Set<String> users = activeConversations.get(conversationId);

        if (users != null) {
            users.remove(username);
        }
    }

    // MAIN MESSAGE EVENT
    public void notifyNewMessage(MessageDTO messageDTO) {

        try {

            Long conversationId = messageDTO.getConversationId();

            Set<String> activeUsers =
                    activeConversations.getOrDefault(
                            conversationId,
                            new HashSet<>()
                    );

            // 1. build MESSAGE payload
            SocketPayloads.MessagePayload messagePayload =
                    new SocketPayloads.MessagePayload(
                            messageDTO.getId(),
                            messageDTO.getConversationId(),
                            messageDTO.getSender(),
                            messageDTO.getContent(),
                            messageDTO.getTimestamp(),
                            messageDTO.getAttachments()
                                    .stream()
                                    .map(a -> a.getFileUrl())
                                    .toList()
                    );

            String messageJson = objectMapper.writeValueAsString(messagePayload);

            // 2. send LIVE message to active users
            for (String username : activeUsers) {
                sendRaw(username, messageJson);
            }

            // 3. send NOTIFICATIONS to inactive users
            notifyInactiveUsers(conversationId, messagePayload, activeUsers);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // NOTIFICATIONS
    private void notifyInactiveUsers(
            Long conversationId,
            SocketPayloads.MessagePayload messagePayload,
            Set<String> activeUsers
    ) {

        try {

            List<String> allMembers =
                    getConversationMembers(conversationId);

            for (String user : allMembers) {

                if (!activeUsers.contains(user)) {

                    SocketPayloads.NotificationPayload notification =
                            new SocketPayloads.NotificationPayload(
                                    conversationId,
                                    "New message",
                                    messagePayload.content,
                                    messagePayload.messageId,
                                    null,
                                    messagePayload.sender
                            );

                    String json = objectMapper.writeValueAsString(notification);

                    sendRaw(user, json);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // SEND TO SINGLE USER
    private void sendToUser(String username, Object message) {

        WebSocketSession session = userSessions.get(username);

        if (session != null && session.isOpen()) {

            try {
                session.sendMessage(
                        new TextMessage(
                                objectMapper.writeValueAsString(message)
                        )
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // GET MEMBERS (stub - zamijeni servisom)
    private List<String> getConversationMembers(Long conversationId) {

        // TODO: zamijeni sa ConversationMemberService / DB query
        return new ArrayList<>();
    }
}