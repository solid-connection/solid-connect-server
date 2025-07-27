package com.example.solidconnection.chat.dto;

import java.time.ZonedDateTime;

public record ChatAttachmentResponse(
        long id,
        boolean isImage,
        String url,
        String thumbnailUrl,
        ZonedDateTime createdAt
) {

    public static ChatAttachmentResponse of(long id, boolean isImage, String url,
                                            String thumbnailUrl, ZonedDateTime createdAt) {
        return new ChatAttachmentResponse(id, isImage, url, thumbnailUrl, createdAt);
    }
}
