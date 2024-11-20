package com.sottie.sottiechat.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

/*
   소켓 통신에 대한 이벤트 이름 정의
 */
@Getter
@NoArgsConstructor
public enum SocketEvent {
    INITIAL_ENTRANCE, // 채팅방 최초 입장
    SEND_MESSAGE, // 메시지 전송
    UPDATE_READ_STATUS // 읽음 상태 업데이트
}
