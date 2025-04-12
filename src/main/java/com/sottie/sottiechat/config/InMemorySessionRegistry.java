package com.sottie.sottiechat.config;

import com.sottie.sottiechat.dto.SessionMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chat Room Session Management Registry In Memory
 */
@Component
@Slf4j
public class InMemorySessionRegistry implements SessionRegistry {

    /*
        Map<roomId, Map<userId, Set<sessionId>>
        채팅방에 들어와있는 유저가 각각 어떤 세션인지 관리하는 구조 (한 유저가 여러 세션으로 접속 가능)
     */
    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, Set<String>>> roomUserSessionMap = new ConcurrentHashMap<>();
    /*
        Map<sessionId, roomId & userId>
        특정 세션 ID가 어떤 채팅방에 들어와있는 유저인지 관리하는 구조
     */
    private final ConcurrentHashMap<String, SessionMeta> sessionMetaMap = new ConcurrentHashMap<>();

    @Override
    public void addSession(Long roomId, Long userId, String sessionId) {
        if (isUserInRoom(roomId, userId)) {
            log.info("[세션 등록] 이미 유저가 접속 중입니다.");
            throw new IllegalStateException("이미 채팅방에 접속 중인 유저입니다.");
        }
        roomUserSessionMap
                .computeIfAbsent(roomId, r -> new ConcurrentHashMap<>())
                .computeIfAbsent(userId, u -> Collections.synchronizedSet(new HashSet<>()))
                .add(sessionId);
        sessionMetaMap.put(sessionId, new SessionMeta(roomId, userId));
    }

    @Override
    public Optional<SessionMeta> getMetaFromSession(String sessionId) {
        return Optional.ofNullable(sessionMetaMap.get(sessionId));
    }

    @Override
    public void removeSession(String sessionId) {
        SessionMeta removed = sessionMetaMap.remove(sessionId);
        if (removed == null) {
            throw new IllegalArgumentException("존재하지 않은 세션에 대한 삭제 시도입니다.");
        }
        Long roomId = removed.roomId();
        Long userId = removed.userId();
        // 삭제한 세션에 대한 채팅방 정보로 부터 유저 정보들을 조회
        ConcurrentHashMap<Long, Set<String>> userMap = roomUserSessionMap.get(roomId);
        if (userMap != null) {
            Set<String> sessions = userMap.get(userId); // 삭제한 세션에 대한 유저 정보로부터 세션 정보들 조회
            if (sessions != null) {
                sessions.remove(sessionId); // 그 세션들 중에서 주어진 세션 삭제
                if (sessions.isEmpty()) {
                    userMap.remove(userId); // 아무 세션도 존재하지 않으면 유저가 없는 것이므로 유저도 삭제
                }
            }
            if (userMap.isEmpty()) {
                roomUserSessionMap.remove(roomId); // 해당 채팅방 내에 아무런 유저도 없으면 채팅방 정보 삭제
            }
        }
    }

    @Override
    public Set<Long> getUsersInRoom(Long roomId) {
        return roomUserSessionMap.getOrDefault(roomId, new ConcurrentHashMap<>()).keySet();
    }

    @Override
    public Set<String> getSessionsInRoom(Long roomId) {
        Set<String> allSessions = new HashSet<>();
        ConcurrentHashMap<Long, Set<String>> userMap = roomUserSessionMap.get(roomId);
        if (userMap != null) {
            for (Set<String> sessions : userMap.values()) {
                allSessions.addAll(sessions);
            }
        }
        return allSessions;
    }

    @Override
    public boolean isUserInRoom(Long roomId, Long userId) {
        return roomUserSessionMap.getOrDefault(roomId, new ConcurrentHashMap<>())
                .containsKey(userId);
    }

    @Override
    public boolean isSessionTracked(String sessionId) {
        return sessionMetaMap.containsKey(sessionId);
    }

    @Override
    public long getConnectorCountByRoom(Long roomId) {
        if (roomId == null)
            return sessionMetaMap.size();
        return sessionMetaMap.values().stream()
                .filter(meta -> meta.roomId().equals(roomId))
                .count();
    }

    public void printStatus() {
        System.out.println("=== Session Registry ===");
        roomUserSessionMap.forEach((roomId, userMap) -> {
            System.out.println("Room: " + roomId);
            userMap.forEach((userId, sessions) -> {
                System.out.println("  User: " + userId + " -> " + sessions);
            });
        });
        System.out.println("SessionMeta:");
        sessionMetaMap.forEach((sessionId, meta) -> {
            System.out.println("  " + sessionId + " -> room: " + meta.roomId() + ", user: " + meta.userId());
        });
    }
}
