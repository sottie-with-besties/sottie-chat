package com.sottie.sottiechat.config;

import com.sottie.sottiechat.dto.MessageRequest;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketInterceptor implements ChannelInterceptor {
    private final MessageService messageService;
    private final StompConnectionHandler connectionHandler;

    @Override
    public boolean preReceive(MessageChannel channel) {
        return ChannelInterceptor.super.preReceive(channel);
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        SimpMessageType messageType = accessor.getMessageType();
        log.info("[인터셉터] Message Type: {}", messageType);

        if (accessor.getCommand() != null) {
            handleMessageByCommand(accessor.getCommand(), accessor);
        }
        // command가 null인 경우는 heart-beat 메시지 or 비-stomp 메시지
        return message;
    }

    private void handleMessageByCommand(StompCommand command, StompHeaderAccessor accessor) {
        switch (command) {
            case CONNECT -> {
                // TODO: 헤더에 JWT 인증된 유저에 대해 연결하기 (웹소켓 연결을 외부에서 한다면 보안적인 문제가 발생 가능성, 그래서 연결할 때도 인증이 필요)
                // TODO: 해당 유저가 이 채팅방에 접속 가능한 유저인지 검증 (API 서버 통신)
                //연결할 때는, 헤더 정보밖에 없어서 CONNECT 상태에서는 읽음 처리 x

                connectionHandler.handleSessionConnected(accessor);
            }
            case SUBSCRIBE -> { // 해당 채팅방 접속
                // TODO: 헤더에 JWT 인증 정보 받아서 유저 정보 가져오기
                Long roomId = parseRoomId(accessor); // sub destination에서 채팅방 id 추출

                /* 임시처리 */
                Long userId = 3L;
                if (accessor.getFirstNativeHeader("userId") != null)
                    userId = Long.parseLong(accessor.getFirstNativeHeader("userId"));

                // 최초 입장 처리
                messageService.enterChatRoom(roomId, MessageRequest.Enter.builder()
                        .userId(userId)
                        .build());

                // 읽음 처리
                messageService.updateLastReadStatus(roomId, MessageRequest.LastRead.builder()
                        .userId(userId)
                        .build());
            }
            case SEND -> {
                // TODO: 헤더에 JWT 인증 정보 받아서 SEND한 유저 정보 가져오기
                // TODO: 암호화?
            }
            case DISCONNECT -> {
                connectionHandler.handleSessionDisconnected(accessor);
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
}
