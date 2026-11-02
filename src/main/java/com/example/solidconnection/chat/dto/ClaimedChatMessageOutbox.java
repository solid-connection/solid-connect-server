package com.example.solidconnection.chat.dto;

public record ClaimedChatMessageOutbox(
        long outboxId,
        String processingToken,
        int attemptCount,
        long roomId,
        ChatMessageSendResponse response
) {
}
