package com.sottie.sottiechat.dto;

import com.sottie.sottiechat.domain.ChatType;
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
        private ChatType chatType;
        private Status status;

        public static MessageResponse.Chat from(MessageRequest.Enter enter) {
            return Chat.builder()
                    .userId(enter.getUserId())
                    .messageId(null)
                    .contents(enter.getUserId() + "님이 채팅방에 참여했습니다.") // TODO: Redis 캐시 또는 RDB로부터 유저 정보 제대로 조회해오기
                    .messageType(MessageType.TEXT)
                    .chatType(ChatType.ENTRANCE)
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
                    .chatType(ChatType.CHAT)
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
