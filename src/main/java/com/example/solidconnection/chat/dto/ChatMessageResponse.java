package com.example.solidconnection.chat.dto;

import java.time.ZonedDateTime;
import java.util.List;

public record ChatMessageResponse(
        long id,
        String content,
        long siteUserId,
        ZonedDateTime createdAt,
        List<ChatAttachmentResponse> attachments
) {

    public static ChatMessageResponse of(long id, String content, long siteUserId,
                                         ZonedDateTime createdAt, List<ChatAttachmentResponse> attachments) {
        return new ChatMessageResponse(id, content, siteUserId, createdAt, attachments);
    }
}
