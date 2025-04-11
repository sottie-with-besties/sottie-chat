package com.sottie.sottiechat.config;

import com.sottie.sottiechat.dto.SessionMeta;

import java.util.Optional;
import java.util.Set;

public interface SessionRegistry {
    void addSession(Long roomId, Long userId, String sessionId);

    void removeSession(String sessionId);

    Set<Long> getUsersInRoom(Long roomId);

    Set<String> getSessionsInRoom(Long roomId);

    boolean isUserInRoom(Long roomId, Long userId);

    boolean isSessionTracked(String sessionId);

    long getConnectorCountByRoom(Long roomId);

    Optional<SessionMeta> getMetaFromSession(String sessionId);

}
