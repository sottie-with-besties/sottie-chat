package com.sottie.sottiechat.dto;

import com.sottie.sottiechat.domain.ChatType;
import com.sottie.sottiechat.domain.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MessageRequest {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Enter {
        private Long userId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Chat {
        private Long userId;
        private String contents;
        private MessageType messageType;
        private ChatType chatType;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LastRead {
        private Long userId;
        private String messageId;
    }
}
