package com.evolt.chatapp.websocket;

import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.User;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("Novi korisnik povezan: " + session.getId());
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

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("Korisnik se odjavio: " + session.getId());
    }
    public void notifyNewUser(User user) {
        String message = "{ \"type\": \"user\", \"username\": \"" + user.getUsername() + "\" }";
        broadcast(message);
    }

    public void notifyNewMessage(Message mess) {
        String message = "{ \"type\": \"chat\", \"sender\": \"" + mess.getSender().getUsername() + "\", \"content\": \"" + mess.getContent() + "\", \"timestamp\": \"" + mess.getTimestamp() + "\" }";
        broadcast(message);
    }

    // Helper metoda za slanje svima
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



}
