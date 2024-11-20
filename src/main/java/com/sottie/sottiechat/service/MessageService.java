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
import java.util.List;

import static com.sottie.sottiechat.domain.SocketEvent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final RabbitTemplate rabbitTemplate;
    private final MongoTemplate mongoTemplate;
    private static final String SUBSCRIBED_EXCHANGE_NAME = "sottie.chat.exchange";
    private static final String ENTRANCE_ROUTING_KEY = "enter.room.";
    private static final String CHAT_ROUTING_KEY = "*.room.";
    private static final String LAST_READ_ROUTING_KEY = "read.room.";

    public void enterChatRoom(Long roomId, MessageRequest.Enter request) {
        log.info("[채팅방 {}번] 사용자 {} 접속", roomId, request.getUserId());
        if (isAlreadyEnteredChatRoom(roomId, request.getUserId())) {
            return;
        }
        MessageResponse.Chat response = MessageResponse.Chat.from(request);
        sendMessageToSubs(roomId, response, ENTRANCE_ROUTING_KEY, INITIAL_ENTRANCE);
    }

    public void sendChatMessage(Long roomId, MessageRequest.Chat request) {
        if (request.getContents().isBlank()) {
            throw new IllegalStateException("빈 메시지를 전송할 수 없습니다.");
        }
        MessageResponse.Chat response = MessageResponse.Chat.from(request);
        sendMessageToSubs(roomId, response, CHAT_ROUTING_KEY, SEND_MESSAGE);
    }

    public void sendMediaMessage(Long roomId, Long userId, String mediaUrl) {
        sendMessageToSubs(roomId, MessageResponse.Chat.from(MessageRequest.Chat.builder()
                .messageType(getMessageTypeByUrl(mediaUrl))
                .chatType(ChatType.CHAT)
                .contents(mediaUrl)
                .userId(userId)
                .build()), CHAT_ROUTING_KEY, SEND_MESSAGE);
    }

    private MessageType getMessageTypeByUrl(String mediaUrl) {
        if (mediaUrl.contains("photos"))
            return MessageType.IMAGE;
        if (mediaUrl.contains("videos"))
            return MessageType.VIDEO;
        if (mediaUrl.contains("files"))
            return MessageType.FILE;
        return MessageType.TEXT;
    }

    public void updateLastReadStatus(Long roomId, MessageRequest.LastRead lastRead) {
        // 채팅방 접속하는 경우, 최신 채팅 조회
        if (lastRead.getMessageId() == null) {
            String latestChatId = getLatestChatId(roomId, lastRead.getUserId());
            if (latestChatId == null)
                return;
            lastRead.setMessageId(latestChatId);
        }
        ChatMessage message = chatMessageRepository.findById(lastRead.getMessageId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메시지입니다."));

        // 일반 채팅이 아닌 경우 읽음 처리 무시
        if (message.getChatType() != ChatType.CHAT) {
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
            rabbitTemplate.convertAndSend(SUBSCRIBED_EXCHANGE_NAME, LAST_READ_ROUTING_KEY + roomId, new CommonResponse<>(UPDATE_READ_STATUS, build));
        } catch (AmqpException e) {
            log.error("[{}] AMQP Protocol Exception (propagation failure]): {}", LAST_READ_ROUTING_KEY, e.getMessage());
        }
    }

    public List<LastReadStatus> getReadStatusByChatRoom(Long roomId) {
        return mongoTemplate.find(new Query(Criteria.where("chatRoomId").is(roomId)), LastReadStatus.class);
    }

    private void sendMessageToSubs(Long roomId, MessageResponse.Chat response, String routingKey, SocketEvent event) {
        Status status = Status.SUCCESS;
        // 저장하고 저장된 메시지 ID까지 함께 구독자들에게 전파
        ChatMessage saved = null;
        try {
            saved = chatMessageRepository.save(ChatMessage.from(roomId, response, status));
            response.updateMessageId(saved.getId());
            rabbitTemplate.convertAndSend(SUBSCRIBED_EXCHANGE_NAME, routingKey + roomId, new CommonResponse<>(event, response));
        } catch (AmqpException e) {
            status = Status.FAIL;
            if (saved != null) {
                saved.updateStatus(status);
                chatMessageRepository.save(saved);
            }
            log.error("[{}] AMQP Protocol Exception (publish failure): {}", routingKey, e.getMessage());
        }
    }

    public List<MessageResponse.Chat> getChatMessagesByPaging(Long roomId, String cursor, int size) {
        if (size < 1)
            throw new IllegalArgumentException("페이지 개수가 유효하지 않습니다.");

        // TODO: 일별로 response 정렬하기
        if (cursor == null) // 최초 페이지
            return buildMessages(chatMessageRepository.findLatestChat(roomId, size));

        return buildMessages(chatMessageRepository.findChatByPaging(roomId, cursor, size));
    }

    private List<MessageResponse.Chat> buildMessages(List<ChatMessage> chatMessages) {
        return chatMessages.stream().map((c) -> MessageResponse.Chat.builder()
                .messageId(c.getId())
                .contents(c.getContents())
                .userId(c.getUserId())
                .timestamp(c.getTimestamp().toString())
                .messageType(c.getMessageType())
                .chatType(c.getChatType())
                .status(c.getStatus()).build()).toList();
    }

    private boolean isAlreadyEnteredChatRoom(Long roomId, Long senderId) {
        return !chatMessageRepository.findByChatRoomIdAndSenderIdWithEntrance(roomId, senderId).isEmpty();
    }

    private String getLatestChatId(Long roomId, Long userId) {
        return chatMessageRepository.findLatestChatOthers(roomId, userId)
                .map(ChatMessage::getId)
                .orElse(null);
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
