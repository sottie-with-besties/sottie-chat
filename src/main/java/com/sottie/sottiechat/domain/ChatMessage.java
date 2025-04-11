package com.sottie.sottiechat.domain;

import com.sottie.sottiechat.dto.MessageResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_message")
@Getter
@NoArgsConstructor
public class ChatMessage {
    @Id
    private String id;
    private Long roomId;
    private Long userId;
    private String contents;
    private LocalDateTime timestamp;
    private MessageType messageType;
    private Status status;
    private EventType eventType;

    @Builder
    public ChatMessage(Long roomId, Long userId, String contents, MessageType messageType, LocalDateTime timestamp, Status status, EventType eventType) {
        this.roomId = roomId;
        this.userId = userId;
        this.contents = contents;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.status = status;
        this.eventType = eventType;
    }

    public static ChatMessage from(Long roomId, MessageResponse.Chat response, String encodedContents) {
        return ChatMessage.builder()
                .roomId(roomId)
                .userId(response.getUserId())
                .eventType(response.getEventType())
                .messageType(response.getMessageType())
                .timestamp(LocalDateTime.now())
                .status(response.getStatus())
                .contents(encodedContents)
                .build();
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    public void updateContents(String newContents) {
        this.contents = newContents;
    }
}
