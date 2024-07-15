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
    private Long chatRoomId;
    private Long senderId;
    private String content;
    private LocalDateTime timestamp;
    private MessageType messageType;
    private Status status;
    private ChatType chatType;

    @Builder
    public ChatMessage(Long chatRoomId, Long senderId, String content, MessageType messageType, LocalDateTime timestamp, Status status, ChatType chatType) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.status = status;
        this.chatType = chatType;
    }

    public static ChatMessage from(Long roomId, MessageResponse.Chat response, Status status){
        return ChatMessage.builder()
                .chatRoomId(roomId)
                .senderId(response.getSender().getUserId())
                .chatType(response.getChatType())
                .messageType(response.getMessageType())
                .timestamp(LocalDateTime.parse(response.getTimestamp()))
                .status(status)
                .content(response.getContents())
                .build();
    }
}
