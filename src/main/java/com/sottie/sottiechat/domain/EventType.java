package com.sottie.sottiechat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum EventType {
    ENTRANCE, // 최초 입장
    CHAT_IN, // 채팅방 입장
    CHAT,
    CHAT_OUT, // 채팅방 퇴장
    EXIT // 완전 퇴장
}
