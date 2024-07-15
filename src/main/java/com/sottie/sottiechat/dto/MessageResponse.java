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
        private UserInfo sender;
        private String contents;
        private String timestamp;
        private MessageType messageType;
        private ChatType chatType;
        private Status status;

        public static MessageResponse.Chat from(MessageRequest.Enter message) {
            return MessageResponse.Chat.builder()
                    .sender(message.getSender())
                    .contents(message.getSender().getNickname() + "님이 채팅방에 참여했습니다.")
                    .messageType(MessageType.TEXT)
                    .chatType(ChatType.ENTRANCE)
                    .status(Status.SUCCESS)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        public static MessageResponse.Chat from(MessageRequest.Chat message) {
            return Chat.builder()
                    .sender(message.getSender())
                    .contents(message.getContents())
                    .messageType(MessageType.TEXT)
                    .chatType(ChatType.CHAT)
                    .status(Status.SUCCESS)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }
    }
}
