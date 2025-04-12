package com.sottie.sottiechat.service;

import com.sottie.sottiechat.domain.*;
import com.sottie.sottiechat.dto.CommonResponse;
import com.sottie.sottiechat.dto.MessageRequest;
import com.sottie.sottiechat.dto.MessageResponse;
import com.sottie.sottiechat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.sottie.sottiechat.domain.SocketEvent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final MessageQueryService messageQueryService;
    private final CryptoService cryptoService;
    private final RabbitTemplate rabbitTemplate;
    private final MongoTemplate mongoTemplate;
    private static final String SUBSCRIBED_EXCHANGE_NAME = "sottie.chat.exchange";
    private static final String ENTRANCE_ROUTING_KEY = "enter.room.";
    private static final String CHAT_ROUTING_KEY = "*.room.";
    private static final String LAST_READ_ROUTING_KEY = "read.room.";

    public void enterChatRoom(Long roomId, Long userId) {
        log.info("[채팅방 {}번] 사용자 {} 접속", roomId, userId);
        if (messageQueryService.isAlreadyEnteredChatRoom(roomId, userId)) { // 최초 접속이 있는 상태라면
            MessageResponse.Chat response = MessageResponse.Chat.from(userId, null, EventType.CHAT_IN);
            propagateMessageToSubs(roomId, response, ENTRANCE_ROUTING_KEY, CHAT_ROOM_IN);
            return;
        }
        MessageResponse.Chat response = MessageResponse.Chat.from(userId, userId + "님이 채팅방에 입장했습니다.", EventType.ENTRANCE);
        propagateMessageToSubs(roomId, response, ENTRANCE_ROUTING_KEY, INITIAL_ENTRANCE);
    }

    public void exitChatRoom(Long roomId, Long userId) {
        // TODO: 완전 퇴장 처리 구현
        MessageResponse.Chat response = MessageResponse.Chat.from(userId, null, EventType.CHAT_OUT);
        propagateMessageToSubs(roomId, response, CHAT_ROUTING_KEY, CHAT_ROOM_OUT);
    }

    public void sendChatMessage(Long roomId, MessageRequest.Chat request) {
        if (request.getContents().isBlank()) {
            throw new IllegalStateException("빈 메시지를 전송할 수 없습니다.");
        }
        MessageResponse.Chat response = MessageResponse.Chat.from(request);
        propagateMessageToSubs(roomId, response, CHAT_ROUTING_KEY, SEND_MESSAGE);
    }

    public void sendMediaMessage(Long roomId, Long userId, String mediaUrl) {
        propagateMessageToSubs(roomId, MessageResponse.Chat.from(MessageRequest.Chat.builder()
                .userId(userId)
                .messageType(MessageType.getMessageTypeByUrl(mediaUrl))
                .eventType(EventType.CHAT)
                .contents(mediaUrl)
                .build()), CHAT_ROUTING_KEY, SEND_MESSAGE);
    }

    public void resendFailureMessage(Long roomId, Long userId, MessageRequest.Resend resend) {
        if (!deleteMessage(roomId, userId, resend.getMessageId())) {
            throw new IllegalStateException("메시지 삭제에 실패했습니다.");
        }

        if (resend.getResendType() == ResendType.RESEND) { // 재전송이라면 메시지 새롭게 저장 및 propagation
            ChatMessage message = messageQueryService.getMessage(resend.getMessageId());

            MessageResponse.Chat response = MessageResponse.Chat.from(userId, message.getContents(), EventType.CHAT);
            propagateMessageToSubs(roomId, response, CHAT_ROUTING_KEY, SEND_MESSAGE);
        }
    }

    public boolean deleteMessage(Long roomId, Long userId, String messageId) {
        Query query = new Query(Criteria
                .where("roomId").is(roomId)
                .and("userId").is(userId)
                .and("_id").is(messageId)
                .and("status").is("FAIL"));
        return mongoTemplate.remove(query, ChatMessage.class).getDeletedCount() > 0;
    }

    public void updateLastReadStatus(Long roomId, MessageRequest.LastRead lastRead) {
        // 채팅방 접속하는 경우, 최신 채팅 조회
        if (lastRead.getMessageId() == null) {
            String latestChatId = messageQueryService.getLatestChatId(roomId, lastRead.getUserId());
            if (latestChatId == null)
                return;
            lastRead.setMessageId(latestChatId);
        }
        ChatMessage message = messageQueryService.getMessage(lastRead.getMessageId());
        // 일반 채팅이 아닌 경우 읽음 처리 무시
        if (message.getEventType() != EventType.CHAT) {
            log.warn("[메시지 읽음 처리]: ChatType이 CHAT이 아님");
            return;
        }

        // DB에 읽음 상태 업데이트
        if (!updateLastRead(roomId, lastRead.getMessageId(), lastRead.getUserId())) {
            log.warn("[메시지 읽음 처리]: DB에 상태 업데이트 실패");
            return;
        }

        // 읽음 상태 propagate
        try {
            MessageResponse.LastRead build = MessageResponse.LastRead.builder()
                    .lastReadMessageId(lastRead.getMessageId())
                    .readTimeStamp(LocalDateTime.now().toString())
                    .userId(lastRead.getUserId()).build();
            rabbitTemplate.convertAndSend(SUBSCRIBED_EXCHANGE_NAME, LAST_READ_ROUTING_KEY + roomId, new CommonResponse<>(UPDATE_READ_STATUS, roomId, build));
        } catch (AmqpException e) {
            log.error("[{}] AMQP Protocol Exception (propagation failure]): {}", LAST_READ_ROUTING_KEY, e.getMessage());
        }
    }

    private void propagateMessageToSubs(Long roomId, MessageResponse.Chat response, String routingKey, SocketEvent event) {
        ChatMessage saved = null;
        try {
            saved = chatMessageRepository.save(ChatMessage.from(roomId, response, response.getContents() != null ? cryptoService.encodeAES(response.getContents()) : null));
            response.updateMessageId(saved.getId());

            rabbitTemplate.convertAndSend(SUBSCRIBED_EXCHANGE_NAME, routingKey + roomId, new CommonResponse<>(event, roomId, response));
        } catch (AmqpException e) {
            if (saved != null) {
                saved.updateStatus(Status.FAIL);
                chatMessageRepository.save(saved);
            }
            log.error("[{}] AMQP Protocol Exception (publish failure): {}", routingKey, e.getMessage());
        } catch (Exception e) {
            log.error("[{}] Exception occurred: {}", routingKey, e.getMessage());
            throw new IllegalStateException("메시지 전파 중 예기치 못한 오류가 발생했습니다.");
        }
    }

    private boolean updateLastRead(Long roomId, String messageId, Long userId) {
        // 이미 읽은 처리된 메시지면 무시
        if (mongoTemplate.exists(new Query(Criteria
                        .where("chatRoomId").is(roomId)
                        .and("userId").is(userId)
                        .and("messageId").is(messageId)),
                LastReadStatus.class)) {
            log.warn("[메시지 읽음 처리]: 이미 읽은 메시지");
            return false;
        }
        Query query = new Query(Criteria
                .where("chatRoomId").is(roomId)
                .and("userId").is(userId));

        Update update = new Update();
        LocalDateTime now = LocalDateTime.now();
        update.set("messageId", messageId);
        update.set("readTimestamp", now);
        return mongoTemplate.upsert(query, update, LastReadStatus.class)
                .wasAcknowledged();
    }
}
