package com.sottie.sottiechat.repository;

import com.sottie.sottiechat.domain.ChatMessage;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    @Query("{'chatRoomId' : ?0, 'senderId' : ?1, 'chatType' : 'ENTRANCE'}")
    List<ChatMessage> findByChatRoomIdAndSenderIdWithEntrance(Long chatRoomId, Long senderId);

    @Aggregation(
            pipeline = {
                    "{ $match : { 'chatRoomId' : ?0 } }",
                    "{ $sort : { '_id' : -1 } }",
                    "{ $limit : ?1 }"
            }
    )
    List<ChatMessage> findLatestChat(Long chatRoomId, int limit);

    @Aggregation(
            pipeline = {
                    "{ $match : { '_id' : { '$lt' : {'$oid' : ?1 } }, 'chatRoomId' : ?0 } }", // _id 비교는 ObjectId() 비교를 위해 $oid 사용
                    "{ $sort : { '_id' : -1 } }",
                    "{ $limit : ?2 }"
            }
    )
    List<ChatMessage> findChatByPaging(Long chatRoomId, String lastId, int limit);
}
