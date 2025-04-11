package com.sottie.sottiechat.dto;

import com.sottie.sottiechat.domain.SocketEvent;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommonResponse<T> {
    private SocketEvent event;
    private Long roomId;
    private T data;

    public CommonResponse(SocketEvent event, Long roomId, T data) {
        this.event = event;
        this.roomId = roomId;
        this.data = data;
    }
}
