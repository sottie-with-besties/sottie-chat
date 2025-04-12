package com.sottie.sottiechat.dto;

import com.sottie.sottiechat.domain.EventType;
import com.sottie.sottiechat.domain.MessageType;
import com.sottie.sottiechat.domain.ResendType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MessageRequest {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Chat {
        private Long userId;
        private String contents;
        private MessageType messageType;
        private EventType eventType;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LastRead {
        private Long userId;
        private String messageId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Resend {
        private String messageId;
        private ResendType resendType;
    }
}
