package com.evolt.chatapp.websocket;

public class WebSocketEvent<T> {
    private final String eventType;
    private final T payload;

    public WebSocketEvent(String eventType, T payload) {
        this.eventType = eventType;
        this.payload = payload;
    }

    public String getEventType() { return eventType; }
    public T getPayload() { return payload; }
}