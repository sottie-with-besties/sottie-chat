package com.sottie.sottiechat.service;

import com.sottie.sottiechat.domain.ChatMessage;
import com.sottie.sottiechat.domain.LastReadStatus;
import com.sottie.sottiechat.dto.MessageResponse;
import com.sottie.sottiechat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageQueryService {
    private final ChatMessageRepository chatMessageRepository;
    private final CryptoService cryptoService;
    private final MongoTemplate mongoTemplate;

    public ChatMessage getMessage(String messageId) {
        return chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메시지입니다."));
    }

    public List<MessageResponse.Chat> getChatMessagesByPaging(Long roomId, String cursor, int size) {
        if (size < 1)
            throw new IllegalArgumentException("페이지 개수가 유효하지 않습니다.");

        // TODO: 일별로 response 정렬하기
        if (cursor == null) // 최초 페이지
            return buildMessages(chatMessageRepository.findLatestChat(roomId, size));

        return buildMessages(chatMessageRepository.findChatByPaging(roomId, cursor, size));
    }

    private List<MessageResponse.Chat> buildMessages(List<ChatMessage> chatMessages) {
        return chatMessages.stream().map((c) -> MessageResponse.Chat.builder()
                .messageId(c.getId())
                .contents(cryptoService.decodeAES(c.getContents()))
                .userId(c.getUserId())
                .timestamp(c.getTimestamp().toString())
                .messageType(c.getMessageType())
                .chatType(c.getChatType())
                .status(c.getStatus()).build()).toList();
    }

    public List<LastReadStatus> getReadStatusByChatRoom(Long roomId) {
        return mongoTemplate.find(new Query(Criteria.where("chatRoomId").is(roomId)), LastReadStatus.class);
    }

    public String getLatestChatId(Long roomId, Long userId) {
        return chatMessageRepository.findLatestChatOthers(roomId, userId)
                .map(ChatMessage::getId)
                .orElse(null);
    }

    public boolean isAlreadyEnteredChatRoom(Long roomId, Long senderId) {
        return !chatMessageRepository.findByChatRoomIdAndSenderIdWithEntrance(roomId, senderId).isEmpty();
    }
}
