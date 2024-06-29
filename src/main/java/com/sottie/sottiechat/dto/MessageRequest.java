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
        private UserInfo sender;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Chat {
        private UserInfo sender;
        private String contents;
        private MessageType messageType;
        private ChatType chatType;
    }
}
