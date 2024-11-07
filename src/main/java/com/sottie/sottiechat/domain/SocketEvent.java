package com.sottie.sottiechat.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

/*
   소켓 통신에 대한 이벤트 이름 정의
 */
@Getter
@NoArgsConstructor
public enum SocketEvent {
    SEND_MESSAGE,
    UPDATE_READ_STATUS
}
