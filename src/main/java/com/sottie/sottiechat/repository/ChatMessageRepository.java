package com.sottie.sottiechat.repository;

import com.sottie.sottiechat.domain.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    @Query("{'chatRoomId' : ?0, 'senderId' : ?1, 'chatType' : 'ENTRANCE'}")
    List<ChatMessage> findByChatRoomIdAndSenderIdWithEntrance(Long chatRoomId, Long senderId);
}
