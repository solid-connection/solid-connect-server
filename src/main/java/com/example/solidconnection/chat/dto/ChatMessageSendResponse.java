package com.example.solidconnection.chat.dto;

import com.example.solidconnection.chat.domain.ChatMessage;

public record ChatMessageSendResponse(
        long messageId,
        String content,
        long senderId
) {

    public static ChatMessageSendResponse from(ChatMessage chatMessage) {
        return new ChatMessageSendResponse(
                chatMessage.getId(),
                chatMessage.getContent(),
                chatMessage.getSenderId()
        );
    }

}
