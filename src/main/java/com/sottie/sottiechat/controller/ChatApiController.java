package com.sottie.sottiechat.controller;

import com.sottie.sottiechat.domain.ChatMessage;
import com.sottie.sottiechat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatApiController {
    private final MessageService messageService;

    @GetMapping("/api/chats/{chatRoomId}")
    public ResponseEntity<List<ChatMessage>> getChatMessageByPaging(
            @PathVariable("chatRoomId") Long chatRoomId,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam("size") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(messageService.getChatMessagesByPaging(chatRoomId, cursor, size));
    }
}
