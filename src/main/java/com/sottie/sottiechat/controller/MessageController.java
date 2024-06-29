package com.sottie.sottiechat.controller;

import com.sottie.sottiechat.dto.MessageRequest;
import com.sottie.sottiechat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @MessageMapping("chat.enter.{roomId}")
    public void enterChatRoom(@DestinationVariable("roomId") Long roomId, @Payload MessageRequest.Enter message) {
        messageService.enterChatRoom(roomId, message);
    }
}
