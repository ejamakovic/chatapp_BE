package com.evolt.chatapp.websocket;

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
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(ObjectMapper objectMapper) {
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
                    new SocketPayloads.UserPayload(userDTO)
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
                        new SocketPayloads.UserLeavePayload(username)
                );

                broadcast(message);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyNewMessage(MessageDTO messageDTO) {
        try {
            String message = objectMapper.writeValueAsString(
                    new SocketPayloads.MessagePayload(
                            messageDTO.getId(),
                            messageDTO.getSender(),
                            messageDTO.getReceiver(),
                            messageDTO.getContent(),
                            messageDTO.getTimestamp(),
                            messageDTO.getAttachments()
                    )
            );

            System.out.println("Receiver: " + messageDTO.getReceiver()) ;
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
                    new SocketPayloads.PrivateRequestPayload(sender, receiver)
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
}