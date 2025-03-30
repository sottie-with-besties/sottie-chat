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
    private ChatType chatType;

    @Builder
    public ChatMessage(Long roomId, Long userId, String contents, MessageType messageType, LocalDateTime timestamp, Status status, ChatType chatType) {
        this.roomId = roomId;
        this.userId = userId;
        this.contents = contents;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.status = status;
        this.chatType = chatType;
    }

    public static ChatMessage from(Long roomId, MessageResponse.Chat response, Status status, String encodedContents) {
        return ChatMessage.builder()
                .roomId(roomId)
                .userId(response.getUserId())
                .chatType(response.getChatType())
                .messageType(response.getMessageType())
                .timestamp(LocalDateTime.now())
                .status(status)
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
