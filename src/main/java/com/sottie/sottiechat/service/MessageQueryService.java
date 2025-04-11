package com.sottie.sottiechat.service;

import com.sottie.sottiechat.domain.ChatMessage;
import com.sottie.sottiechat.domain.LastReadStatus;
import com.sottie.sottiechat.dto.ChatMessageDateGroup;
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
    private static final int CURRENT_PAGING_SIZE = 30;

    public ChatMessage getMessage(String messageId) {
        return chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메시지입니다."));
    }

    public List<ChatMessageDateGroup> getChatMessagesByPaging(Long userId, Long roomId, String cursor, boolean useDecode) {
        if (cursor == null) // 최신 채팅 조회
            return buildMessages(chatMessageRepository.findLatestChatByPagingGroupByDate(userId, roomId, CURRENT_PAGING_SIZE), useDecode);

        return buildMessages(chatMessageRepository.findChatByPagingGroupByDate(userId, roomId, cursor, CURRENT_PAGING_SIZE), useDecode);
    }

    private List<ChatMessageDateGroup> buildMessages(List<ChatMessageDateGroup> chatMessages, boolean useDecode) {
        chatMessages.stream()
                .flatMap(dateGroup -> dateGroup.getChats().stream())
                .filter(chatMessage -> useDecode)
                .forEach(chatMessage -> chatMessage.updateContents(chatMessage.getContents() != null ? cryptoService.decodeAES(chatMessage.getContents()) : null));
        return chatMessages;
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
        return !chatMessageRepository.findByChatRoomIdAndUserIdWithEntrance(roomId, senderId).isEmpty();
    }
}
