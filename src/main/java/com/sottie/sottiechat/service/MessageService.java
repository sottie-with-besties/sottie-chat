package com.sottie.sottiechat.service;

import com.sottie.sottiechat.domain.ChatMessage;
import com.sottie.sottiechat.domain.Status;
import com.sottie.sottiechat.dto.MessageRequest;
import com.sottie.sottiechat.dto.MessageResponse;
import com.sottie.sottiechat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final RabbitTemplate rabbitTemplate;
    private static final String SUBSCRIBED_EXCHANGE_NAME = "sottie.chat.exchange";
    private static final String ENTRANCE_ROUTING_KEY = "enter.room.";
    private static final String CHAT_ROUTING_KEY = "*.room.";

    public void enterChatRoom(Long roomId, MessageRequest.Enter request) {
        if (isAlreadyEnteredChatRoom(roomId, request.getSender().getUserId())) {
            log.error("채팅방 " + roomId + "에서" + "사용자" + request.getSender().getUserId() + "가 이미 입장함.");
            throw new IllegalStateException("이미 채팅방에 입장한 사용자입니다.");
        }

        MessageResponse.Chat response = MessageResponse.Chat.from(request);
        sendMessageToSubs(roomId, response, ENTRANCE_ROUTING_KEY);
    }

    public void sendChatMessage(Long roomId, MessageRequest.Chat message) {
        /*
            TODO: 허용된 문자 & 형식 검사, 메시지 길이 제한, 도배 & 중복 방지(Rate Limit) -> 후순위
             데이터 암호화 & NoSQL에 대한 SQL Injection 방지 -> 데이터 암호화는 중요할 수 있음
         */
        if (message.getContents().isBlank()) {
            throw new IllegalStateException("빈 메시지를 전송할 수 없습니다.");
        }
        MessageResponse.Chat response = MessageResponse.Chat.from(message);
        sendMessageToSubs(roomId, response, CHAT_ROUTING_KEY);
    }

    private void sendMessageToSubs(Long roomId, MessageResponse.Chat response, String routingKey) {
        Status status = Status.SUCCESS;
        try {
            rabbitTemplate.convertAndSend(SUBSCRIBED_EXCHANGE_NAME, routingKey + roomId, response);
        } catch (AmqpException e) {
            status = Status.FAIL;
            log.error("[{}] AMQP Protocol Exception (publish failure): {}", routingKey, e.getMessage());
        } finally {
            chatMessageRepository.save(ChatMessage.from(roomId, response, status));
        }
    }

    public List<ChatMessage> getChatMessagesByPaging(Long chatRoomId, String cursor, int size) {
        if (size < 1)
            throw new IllegalArgumentException("페이지 개수가 유효하지 않습니다.");

        if (cursor == null) // 최초 페이지
            return chatMessageRepository.findLatestChat(chatRoomId, size);

        return chatMessageRepository.findChatByPaging(chatRoomId, cursor, size);
    }

    private boolean isAlreadyEnteredChatRoom(Long roomId, Long senderId) {
        return !chatMessageRepository.findByChatRoomIdAndSenderIdWithEntrance(roomId, senderId).isEmpty();
    }
}
