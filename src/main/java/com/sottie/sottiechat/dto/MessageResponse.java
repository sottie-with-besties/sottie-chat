package com.sottie.sottiechat.dto;

import com.sottie.sottiechat.domain.ChatType;
import com.sottie.sottiechat.domain.MessageType;
import com.sottie.sottiechat.domain.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    }
}
