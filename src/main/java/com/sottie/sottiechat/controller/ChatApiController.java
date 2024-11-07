package com.sottie.sottiechat.controller;

import com.sottie.sottiechat.domain.LastReadStatus;
import com.sottie.sottiechat.dto.MessageResponse;
import com.sottie.sottiechat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatApiController {
    private final MessageService messageService;

    @GetMapping("/api/chats/{chatRoomId}")
    public ResponseEntity<List<MessageResponse.Chat>> getChatMessageByPaging(
            @PathVariable("chatRoomId") Long chatRoomId,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam("size") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(messageService.getChatMessagesByPaging(chatRoomId, cursor, size));
    }

    @GetMapping("/api/chats/readStatus/{chatRoomId}")
    public ResponseEntity<List<LastReadStatus>> getReadStatusByChatRoom(
            @PathVariable("chatRoomId") Long chatRoomId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(messageService.getReadStatusByChatRoom(chatRoomId));
    }
}
