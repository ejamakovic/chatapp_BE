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
        if (query == null) return false;

        String token = null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && "token".equals(
                    URLDecoder.decode(kv[0], StandardCharsets.UTF_8))) {
                token = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                break;
            }
        }

        if (token == null || token.isBlank() || !jwtService.isTokenValid(token)) {
            return false;
        }

        String username = jwtService.extractUsername(token);
        Long id = Long.valueOf(jwtService.extractUserId(token));

        if (username == null) return false;

        attributes.put("username", username);
        attributes.put("id", id);
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {}
}