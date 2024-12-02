package com.sottie.sottiechat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocketConnector {
    private Long userId;
    private Long roomId;
}
