package com.sottie.sottiechat.domain;

import com.sottie.sottiechat.repository.ChatMessageRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChatMessageTest {

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @AfterEach
    void clean(){
        chatMessageRepository.deleteAll();
    }

    @Test
    void insertionTest(){
        ChatMessage message = ChatMessage.builder()
                .chatRoomId(1L)
                .senderId(2L)
                .content("Hello World")
                .messageType(MessageType.TEXT)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        ChatMessage chatMessage = chatMessageRepository.findById(saved.getId()).get();
        Assertions.assertThat(chatMessage.getContent()).isEqualTo("Hello World");
        Assertions.assertThat(chatMessage.getSenderId()).isEqualTo(2L);
        Assertions.assertThat(chatMessage.getChatRoomId()).isEqualTo(1L);
        Assertions.assertThat(chatMessage.getMessageType()).isEqualTo(MessageType.TEXT);
        Assertions.assertThat(chatMessage.getStatus()).isEqualTo(Status.SUCCESS);
        System.out.println(chatMessage.getTimestamp());
        System.out.println(chatMessage.getId());

    }
}