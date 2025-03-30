package com.sottie.sottiechat.repository;

import com.sottie.sottiechat.domain.ChatMessage;
import com.sottie.sottiechat.dto.ChatMessageDateGroup;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    @Query("{'roomId' : ?0, 'userId' : ?1, 'chatType' : 'ENTRANCE'}")
    List<ChatMessage> findByChatRoomIdAndSenderIdWithEntrance(Long roomId, Long senderId);

    @Aggregation(
            pipeline = {
                    "{ $match : { 'roomId' : ?1, " +
                            "$or : [ {'userId' : ?0 }, {'userId' : { $ne : ?0 }, 'status' : 'SUCCESS' } ]" +
                            " } }",
                    "{ $sort : { '_id' : -1 } }",
                    "{ $limit : ?2 }"
            }
    )
    List<ChatMessage> findLatestChat(Long userId, Long roomId, int limit); // 최초 채팅 목록 조회

    @Aggregation(
            pipeline = {
                    "{ $match : { 'roomId' : ?0, 'userId' : {'$ne' : ?1 } } }",
                    "{ $sort : { '_id' : -1 } }",
                    "{ $limit : 1 }"
            }
    )
    Optional<ChatMessage> findLatestChatOthers(Long roomId, Long userId);

    @Aggregation(
            pipeline = {
                    "{ $match : { '_id' : { '$lt' : {'$oid' : ?2 } }, 'roomId' : ?1, " + // _id 비교는 ObjectId() 비교를 위해 $oid 사용
                            "$or: [ {'userId' : ?0 }, {'userId' : { $ne : ?0 }, 'status' : 'SUCCESS' } ]" +
                            " } }",
                    "{ $sort : { '_id' : -1 } }",
                    "{ $limit : ?3 }"
            }
    )
    List<ChatMessage> findChatByPaging(Long userId, Long roomId, String lastId, int limit);

    @Aggregation(pipeline = {
            // 조건에 대해 필터링한 데이터를 조회
            "{ $match : { '_id' : { '$lt' : {'$oid' : ?2 } }, 'roomId' : ?1, " +
                    "$or: [ {'userId' : ?0 }, {'userId' : { $ne : ?0 }, 'status' : 'SUCCESS' } ]" +
                    " } }",
            // 먼저 시간순으로 내림차순 정렬
            "{ $sort: { 'timestamp': -1 } }",
            // Y-m-d 포맷으로 timestamp 필드에 대한 날짜로 그룹핑해서 documents들을 리스트로 수집
            "{ $group: { '_id': { '$dateToString': { 'format': '%Y-%m-%d', 'date': '$timestamp' } }, 'chats': { '$push': '$$ROOT' } } }",
            // 최종 결과에서 _id 필드를 date필드로 변환
            "{ $project: { '_id': 0, 'date': '$_id', 'chats': 1 } }", // _id 필드를 제외(0)하고, _id 필드 값을 date 필드로 변경, chats 필드는 포함하겠다는 의미
            // 그룹화된 날짜 자체도 내림차순 정렬
            "{ $sort: { 'date': -1 } }",
            "{ $limit : ?3 }"
    })
    List<ChatMessageDateGroup> findChatByPagingGroupByDate(Long userId, Long roomId, String lastId, int limit);

    @Aggregation(pipeline = {
            // 조건에 대해 필터링한 데이터를 조회
            "{ $match : { 'roomId' : ?1, " +
                    "$or: [ {'userId' : ?0 }, {'userId' : { $ne : ?0 }, 'status' : 'SUCCESS' } ]" +
                    " } }",
            // 먼저 시간순으로 내림차순 정렬
            "{ $sort: { 'timestamp': -1 } }",
            // Y-m-d 포맷으로 timestamp 필드에 대한 날짜로 그룹핑해서 documents들을 리스트로 수집
            "{ $group: { '_id': { '$dateToString': { 'format': '%Y-%m-%d', 'date': '$timestamp' } }, 'chats': { '$push': '$$ROOT' } } }",
            // 최종 결과에서 _id 필드를 date필드로 변환
            "{ $project: { '_id': 0, 'date': '$_id', 'chats': 1 } }",
            // 그룹핑된 날짜 자체도 내림차순 정렬
            "{ $sort: { 'date': -1 } }",
            "{ $limit : ?2 }"
    })
    List<ChatMessageDateGroup> findLatestChatByPagingGroupByDate(Long userId, Long roomId, int limit);
}
