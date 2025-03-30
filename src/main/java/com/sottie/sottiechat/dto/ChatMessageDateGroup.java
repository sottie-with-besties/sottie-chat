package com.sottie.sottiechat.dto;

import com.sottie.sottiechat.domain.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDateGroup {
    private String date;
    private List<ChatMessage> chats;
}
