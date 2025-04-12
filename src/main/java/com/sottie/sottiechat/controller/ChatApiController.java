package com.sottie.sottiechat.controller;

import com.sottie.sottiechat.domain.LastReadStatus;
import com.sottie.sottiechat.dto.ChatMessageDateGroup;
import com.sottie.sottiechat.dto.MessageRequest;
import com.sottie.sottiechat.service.MediaService;
import com.sottie.sottiechat.service.MessageQueryService;
import com.sottie.sottiechat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatApiController {
    private final MessageQueryService messageQueryService;
    private final MessageService messageService;
    private final MediaService mediaService;

    @GetMapping("/api/chat/{roomId}")
    public ResponseEntity<List<ChatMessageDateGroup>> getChatMessageByPaging(
            @PathVariable("roomId") Long roomId,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "userId") Long userId // 조회 대상자, TODO: 토큰 헤더로 변경
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(messageQueryService.getChatMessagesByPaging(userId, roomId, cursor, true));
    }

    @GetMapping("/api/chat/{roomId}/readStatus")
    public ResponseEntity<List<LastReadStatus>> getReadStatusByChatRoom(
            @PathVariable("roomId") Long roomId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(messageQueryService.getReadStatusByChatRoom(roomId));
    }

    @PostMapping(value = "/api/chat/{roomId}/media/{userId}", consumes = {"multipart/form-data"})
    public ResponseEntity<Void> uploadMediaFile(
            @PathVariable("roomId") Long roomId,
            @PathVariable("userId") Long userId, // TODO: JWT 헤더로 변경
            @RequestPart(value = "files") List<MultipartFile> files
    ) {
        mediaService.uploadMediaFiles(roomId, userId, files);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/chat/{roomId}/resend/{userId}")
    public ResponseEntity<Void> resendFailureMessage(
            @PathVariable("roomId") Long roomId,
            @PathVariable("userId") Long userId,
            @RequestBody MessageRequest.Resend resend
    ) {
        messageService.resendFailureMessage(roomId, userId, resend);
        return ResponseEntity.ok().build();
    }
}
