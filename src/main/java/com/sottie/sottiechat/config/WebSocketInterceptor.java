package com.sottie.sottiechat.config;

import com.sottie.sottiechat.dto.MessageRequest;
import com.sottie.sottiechat.dto.SessionMeta;
import com.sottie.sottiechat.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketInterceptor implements ChannelInterceptor {
    private final MessageService messageService;
    private final SessionRegistry sessionRegistry;

    @Override
    public boolean preReceive(MessageChannel channel) {
        return ChannelInterceptor.super.preReceive(channel);
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        SimpMessageType messageType = accessor.getMessageType();
        log.info("[Interceptor] Message Type: {}", messageType);

        if (accessor.getCommand() != null) { // command가 null인 경우는 heart-beat 메시지 or non-stomp 메시지
            handleMessageByCommand(accessor.getCommand(), accessor);
        }

        return message;
    }

    private void handleMessageByCommand(StompCommand command, StompHeaderAccessor accessor) {
        switch (command) {
            case CONNECT -> { // 소켓 연결
                // TODO: 토큰 헤더 방식 전환하기
                if (accessor.getFirstNativeHeader("userId") != null) {
                    long userId = Long.parseLong(accessor.getFirstNativeHeader("userId"));
                    log.info("[CONNECT] 연결 유저 : {}", userId);
                    accessor.getSessionAttributes().put("userId", userId);
                } else {
                    throw new IllegalArgumentException("유저 정보가 존재하지 않습니다.");
                }
            }
            case SUBSCRIBE -> { // 채팅방 접속
                Long roomId = parseRoomId(accessor); // destination 헤더에서 채팅방 ID 추출
                Long userId = (Long) accessor.getSessionAttributes().get("userId"); // 세션에 저장한 userId 가져오기
                if (userId == null)
                    throw new IllegalStateException("채팅방 접속 중 유저 정보를 가져올 수 없습니다.");
                String sessionId = getSessionId(accessor);
                log.info("[SUBSCRIBE] 접속 유저 ID: {} ", userId);
                log.info("[SUBSCRIBE] 접속 채팅방 ID : {} ", roomId);
                log.info("[SUBSCRIBE] 접속 세션 ID: {}", sessionId);

                // 세션 등록
                sessionRegistry.addSession(roomId, userId, sessionId);
                log.info("[Session Handler] Connection Completed");

                // 입장 처리
                messageService.enterChatRoom(roomId, userId);

                // 읽음 처리
                messageService.updateLastReadStatus(roomId, MessageRequest.LastRead.builder()
                        .userId(userId)
                        .build());
            }
            case DISCONNECT -> { // 소켓 연결 종료 및 채팅방 종료
                handleSessionDisconnected(accessor);
            }
        }
    }

    private Long parseRoomId(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination != null) {
            Matcher matcher = Pattern.compile(".*\\.(\\d+)$").matcher(destination);
            if (matcher.matches()) {
                return Long.parseLong(matcher.group(1));
            }
        }
        return null;
    }

    private void handleSessionDisconnected(StompHeaderAccessor accessor) {
        String sessionId = getSessionId(accessor);
        SessionMeta meta = sessionRegistry.getMetaFromSession(sessionId).orElseThrow(() -> new IllegalStateException("세션에 대한 메타 정보가 존재하지않음"));
        messageService.exitChatRoom(meta.roomId(), meta.userId());
        sessionRegistry.removeSession(sessionId);
        log.info("[Session Handler] Disconnected to: Session ID: " + sessionId);
    }

    private String getSessionId(StompHeaderAccessor accessor) {
        if (accessor.getSessionId() == null)
            throw new IllegalArgumentException("세션 ID가 존재하지 않습니다.");
        return accessor.getSessionId();
    }

    private void printStompHeader(StompHeaderAccessor accessor) {
        Set<Map.Entry<String, Object>> entrySet = accessor.getMessageHeaders().entrySet();
        entrySet.forEach(e -> {
            System.out.println("Key: " + e.getKey() + ", Value: " + e.getValue().toString());
        });
    }
}
