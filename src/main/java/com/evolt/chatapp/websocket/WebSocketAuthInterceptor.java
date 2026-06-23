package com.evolt.chatapp.websocket;

import com.evolt.chatapp.jwt.JwtService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    public WebSocketAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        String query = request.getURI().getQuery();

        if (query == null || !query.contains("token=")) {
            return false;
        }

        try {
            // Clean parsing to separate token from potential subsequent parameters (&)
            String token = null;
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0 && URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8).equals("token")) {
                    token = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                    break;
                }
            }

            if (token == null || !jwtService.isTokenValid(token)) {
                return false; // Safely drops handshake if token is corrupt or expired
            }

            String username = jwtService.extractUsername(token);
            Long id = Long.valueOf(jwtService.extractUserId(token));

            if (username == null) return false;

            attributes.put("username", username);
            attributes.put("id", id);

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {}
}