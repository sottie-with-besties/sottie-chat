package com.sottie.sottiechat.dto;

import com.sottie.sottiechat.domain.EventType;
import com.sottie.sottiechat.domain.MessageType;
import com.sottie.sottiechat.domain.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class MessageResponse {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Chat {
        private Long userId;
        private String messageId;
        private String contents;
        private String timestamp;
        private MessageType messageType;
        private EventType eventType;
        private Status status;

        public static MessageResponse.Chat from(Long userId, String contents, EventType eventType) {
            return Chat.builder()
                    .userId(userId)
                    .messageId(null)
                    .contents(contents) // TODO: Redis 캐시 또는 RDB로부터 유저 정보 제대로 조회해오기
                    .messageType(MessageType.TEXT)
                    .eventType(eventType)
                    .status(Status.SUCCESS)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        public static MessageResponse.Chat from(MessageRequest.Chat chat) {
            return Chat.builder()
                    .userId(chat.getUserId())
                    .messageId(null)
                    .contents(chat.getContents())
                    .messageType(chat.getMessageType())
                    .eventType(EventType.CHAT)
                    .status(Status.SUCCESS)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        public void updateMessageId(String messageId) {
            this.messageId = messageId;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LastRead {
        private Long userId;
        private String lastReadMessageId;
        private String readTimeStamp;
    }
}
