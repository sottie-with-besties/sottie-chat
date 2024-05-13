package com.sottie.sottiechat.domain;

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
    private Long chatRoomId;
    private Long senderId;
    private String content;
    private LocalDateTime timestamp;
    private MessageType messageType;
    private Status status;

    @Builder
    public ChatMessage(Long chatRoomId, Long senderId, String content, MessageType messageType) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = LocalDateTime.now();
        this.status = Status.SUCCESS;
    }
}
