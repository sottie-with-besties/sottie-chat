package com.sottie.sottiechat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    FILE
}
