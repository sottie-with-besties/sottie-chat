package com.sottie.sottiechat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    FILE,
    EMOTICON;

    public static MessageType getMessageTypeByUrl(String mediaUrl) {
        if (mediaUrl.contains("photos"))
            return IMAGE;
        if (mediaUrl.contains("videos"))
            return VIDEO;
        if (mediaUrl.contains("files"))
            return FILE;
        if (mediaUrl.contains("emoticon"))
            return EMOTICON;
        return TEXT;
    }
}
