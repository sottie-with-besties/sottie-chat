package com.sottie.sottiechat.domain;

import com.sottie.sottiechat.dto.ChatMessageDateGroup;
import com.sottie.sottiechat.repository.ChatMessageRepository;
import com.sottie.sottiechat.service.MessageQueryService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
class ChatMessageTest {

    @Autowired
    ChatMessageRepository chatMessageRepository;
    @Autowired
    MessageQueryService messageQueryService;

    @AfterEach
    void clean() {
        chatMessageRepository.deleteAll();
    }

    @Test
    void insertionTest() {
//        ChatMessage message = ChatMessage.builder()
//                .roomId(1L)
//                .userId(2L)
//                .contents("Hello World")
//                .messageType(MessageType.TEXT)
//                .build();
//
//        ChatMessage saved = chatMessageRepository.save(message);
//
//        ChatMessage chatMessage = chatMessageRepository.findById(saved.getId()).get();
//        Assertions.assertThat(chatMessage.getContents()).isEqualTo("Hello World");
//        Assertions.assertThat(chatMessage.getUserId()).isEqualTo(2L);
//        Assertions.assertThat(chatMessage.getRoomId()).isEqualTo(1L);
//        Assertions.assertThat(chatMessage.getMessageType()).isEqualTo(MessageType.TEXT);
//        Assertions.assertThat(chatMessage.getStatus()).isEqualTo(Status.SUCCESS);
//        System.out.println(chatMessage.getTimestamp());
//        System.out.println(chatMessage.getId());
    }

    @Test
    void getFailureChat() {
        ChatMessage message = ChatMessage.builder()
                .roomId(1L)
                .userId(1L)
                .contents("Hello World")
                .messageType(MessageType.TEXT)
                .eventType(EventType.CHAT)
                .timestamp(LocalDateTime.of(2025, 3, 26, 14, 0))
                .status(Status.FAIL)
                .build();
        ChatMessage message4 = ChatMessage.builder()
                .roomId(1L)
                .userId(1L)
                .contents("Hello World4")
                .messageType(MessageType.TEXT)
                .eventType(EventType.CHAT)
                .timestamp(LocalDateTime.of(2025, 3, 25, 14, 0))
                .status(Status.SUCCESS)
                .build();
        ChatMessage message2 = ChatMessage.builder()
                .roomId(1L)
                .userId(2L)
                .contents("Hello World2")
                .messageType(MessageType.TEXT)
                .eventType(EventType.CHAT)
                .timestamp(LocalDateTime.of(2025, 3, 26, 14, 2))
                .status(Status.SUCCESS)
                .build();
        ChatMessage message3 = ChatMessage.builder()
                .roomId(1L)
                .userId(2L)
                .contents("Hello World3")
                .messageType(MessageType.TEXT)
                .eventType(EventType.CHAT)
                .timestamp(LocalDateTime.of(2025, 3, 27, 14, 0))
                .status(Status.FAIL)
                .build();
        ChatMessage message5 = ChatMessage.builder()
                .roomId(2L)
                .userId(3L)
                .contents("Hello World5")
                .eventType(EventType.CHAT)
                .messageType(MessageType.TEXT)
                .timestamp(LocalDateTime.of(2025, 3, 27, 14, 1))
                .status(Status.FAIL)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        ChatMessage saved2 = chatMessageRepository.save(message2);
        ChatMessage saved3 = chatMessageRepository.save(message3);
        ChatMessage saved4 = chatMessageRepository.save(message4);
        ChatMessage saved5 = chatMessageRepository.save(message5);
        // 5개 중 message3, 5 빼고 다 나와야됨
        List<ChatMessageDateGroup> paging = messageQueryService.getChatMessagesByPaging(1L, 1L, saved3.getId(), false);

//        Assertions.assertThat(paging.size()).isEqualTo(3);


        List<ChatMessageDateGroup> groupByDate = chatMessageRepository.findChatByPagingGroupByDate(1L, 1L, saved3.getId(), 20);

//        for (ChatMessageDateGroup dto:
//             groupByDate) {
//            System.out.println(dto.getDate() + " " + dto.getChats().stream().map(ChatMessage::getTimestamp).toList());
//        }
    }
}