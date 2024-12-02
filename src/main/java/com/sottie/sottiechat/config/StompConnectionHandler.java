package com.sottie.sottiechat.config;

import com.sottie.sottiechat.dto.SocketConnector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
    Chat Room Session Management
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StompConnectionHandler {
    private final ConcurrentHashMap<String, SocketConnector> connectedClients = new ConcurrentHashMap<>();
    /*
        구조를 어떻게 해야할까? 세션 ID, 채팅방 ID, 유저 ID 이 3가지 정보를 조합
     */

    // TODO: Redis 같은 곳에서 분산해서 {사용자 ID : 세션 ID} key:value 구조로 관리
    public void handleSessionConnected(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        if (sessionId == null) {
            throw new IllegalStateException("유효하지 않은 세션 정보");
        }
        log.info("[커넥션] 접근 유저 : " + accessor.getNativeHeader("userId"));
        log.info("[커넥션] 접근 채팅방 : " + accessor.getNativeHeader("roomId"));


        // TODO: 토큰 헤더 정보로부터 유저 정보 가져오기 (API 서버)
        if (accessor.getNativeHeader("userId") == null || accessor.getNativeHeader("roomId") == null) {
            throw new IllegalArgumentException("채팅 접속을 위한 필수 정보가 없습니다.");
        }
        long username = Long.parseLong(accessor.getNativeHeader("userId").get(0));

        long roomId = Long.parseLong(accessor.getNativeHeader("roomId").get(0));

        // TODO: 이 유저가 이 채팅방에 대한 접속 권한이 있는지 검증 (API 서버)
        SocketConnector connection = new SocketConnector(username, roomId);
        if (connectedClients.containsKey(sessionId)) {
            log.info("[커넥션] 접속 상태의 유저: User {}", username);
            return;
        }

        if (connectedClients.containsValue(connection)) {
            log.warn("[커넥션] 중복 접속 시도: User {}", username);
            throw new IllegalStateException("이미 채팅방에 접속 중입니다.");
        }
        connectedClients.put(sessionId, connection);
        log.info("[커넥션] Connected to " + roomId + " : " + username + " (Session ID: " + sessionId + ")");
    }

    public void handleSessionDisconnected(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();

        if (sessionId == null) {
            throw new IllegalStateException("유효하지 않은 세션 정보");
        }
        if (connectedClients.containsKey(sessionId)) {
            SocketConnector removed = connectedClients.remove(sessionId);
            log.info("[커넥션] Disconnected to : " + removed.getRoomId() + " : " + removed.getUserId() + " (Session ID: " + sessionId + ")");
        }
    }

    public Map<String, SocketConnector> getConnectedClients() {
        return connectedClients;
    }

    public long getConnectorCountsByRoom(Long roomId) {
        if (roomId == null)
            return connectedClients.size();
        return connectedClients.values().stream()
                .filter(socketConnector -> socketConnector.getRoomId().equals(roomId))
                .count();
    }
}
