package com.sottie.sottiechat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
    채팅방 접속, 접속종료에 대한 접속자 세션 관리
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StompConnectionListener {
    private final ConcurrentHashMap<String, String> connectedClients = new ConcurrentHashMap<>();

    // TODO: Redis 같은 곳에서 분산해서 {사용자 ID : 세션 ID} key:value 구조로 관리
    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        System.out.println("userId : " +accessor.getNativeHeader("userId"));
        System.out.println("roomId : " +accessor.getNativeHeader("roomId"));
        String username = accessor.getUser() != null ? accessor.getUser().getName() : "Unknown";

        if(accessor.getNativeHeader("userId") != null) {
            username = accessor.getNativeHeader("userId").get(0);
        }
//        if(connectedClients.containsValue(username)){
//            log.warn("이미 접속 중인 유저: {}", username);
//            throw new IllegalStateException("이미 채팅방에 접속 중입니다.");
//        }
        connectedClients.put(username, sessionId);
        System.out.println("Connected: " + username + " (Session ID: " + sessionId + ")");
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        String username = connectedClients.remove(sessionId);
        System.out.println("Disconnected: " + username + " (Session ID: " + sessionId + ")");
    }

    public Map<String, String> getConnectedClients() {
        return connectedClients;
    }
}
