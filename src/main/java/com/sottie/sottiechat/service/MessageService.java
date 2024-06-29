package com.sottie.sottiechat.service;

import com.sottie.sottiechat.domain.ChatMessage;
import com.sottie.sottiechat.domain.ChatType;
import com.sottie.sottiechat.domain.MessageType;
import com.sottie.sottiechat.domain.Status;
import com.sottie.sottiechat.dto.MessageRequest;
import com.sottie.sottiechat.dto.MessageResponse;
import com.sottie.sottiechat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final RabbitTemplate rabbitTemplate;
    private static final String SUBSCRIBED_EXCHANGE_NAME = "sottie.chat.exchange";
    private static final String ENTRANCE_ROUTING_KEY_PREFIX = "enter.room.";

    public void enterChatRoom(Long roomId, MessageRequest.Enter request) {
        if (isAlreadyEnteredChatRoom(roomId, request.getSender().getUserId())) {
            log.error("채팅방 " + roomId + "에서" + "사용자" + request.getSender().getUserId() + "가 이미 입장함.");
            throw new IllegalStateException("이미 채팅방에 입장한 사용자입니다.");
        }

        MessageResponse.Chat response = getEnterChatResponse(request);
        Status status = Status.SUCCESS;
        try {
            sendMessageToSubscribers(roomId, response);
        } catch (AmqpException e) {
            status = Status.FAIL;
        } finally {
            chatMessageRepository.save(buildChatMessage(roomId, response, status));
        }
    }

    private boolean isAlreadyEnteredChatRoom(Long roomId, Long senderId) {
        return !chatMessageRepository.findByChatRoomIdAndSenderIdWithEntrance(roomId, senderId).isEmpty();
    }

    private MessageResponse.Chat getEnterChatResponse(MessageRequest.Enter message) {
        return MessageResponse.Chat.builder()
                .sender(message.getSender())
                .contents(message.getSender().getNickname() + "님이 채팅방에 참여했습니다.")
                .messageType(MessageType.TEXT)
                .chatType(ChatType.ENTRANCE)
                .status(Status.SUCCESS)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    private ChatMessage buildChatMessage(Long roomId, MessageResponse.Chat response, Status status) {
        return ChatMessage.builder()
                .chatRoomId(roomId)
                .senderId(response.getSender().getUserId())
                .chatType(response.getChatType())
                .messageType(response.getMessageType())
                .timestamp(LocalDateTime.parse(response.getTimestamp()))
                .status(status)
                .content(response.getContents())
                .build();
    }

    private void sendMessageToSubscribers(Long roomId, MessageResponse.Chat message) {
        rabbitTemplate.convertAndSend(SUBSCRIBED_EXCHANGE_NAME, ENTRANCE_ROUTING_KEY_PREFIX + roomId, message);
    }
}
