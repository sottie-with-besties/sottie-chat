package com.sottie.sottiechat.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("last_read_status")
@Getter
@NoArgsConstructor
public class LastReadStatus {
    @Id
    private String id;
    private Long chatRoomId;
    private Long userId;
    private String messageId;
    private LocalDateTime readTimestamp;

    @Builder
    public LastReadStatus(Long chatRoomId, Long userId, String messageId, LocalDateTime readTimestamp) {
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.messageId = messageId;
        this.readTimestamp = readTimestamp;
    }
}
