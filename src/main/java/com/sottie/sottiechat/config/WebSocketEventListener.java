package com.sottie.sottiechat.config;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;

/*
    채팅방 접속, 접속종료에 대한 접속자 세션 관리
 */
@Component
public class WebSocketEventListener {
    private final Map<String, String> connectedClients = new HashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        System.out.println("userId : " +accessor.getNativeHeader("userId"));
        System.out.println("roomId : " +accessor.getNativeHeader("roomId"));
        String username = accessor.getUser() != null ? accessor.getUser().getName() : "Unknown";

        connectedClients.put(sessionId, username);
        System.out.println("Connected: " + username + " (Session ID: " + sessionId + ")");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        String username = connectedClients.remove(sessionId);
        System.out.println("Disconnected: " + username + " (Session ID: " + sessionId + ")");
    }

    public Map<String, String> getConnectedClients() {
        return connectedClients;
    }
}
