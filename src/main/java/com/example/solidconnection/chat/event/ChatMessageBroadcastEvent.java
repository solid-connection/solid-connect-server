package com.example.solidconnection.chat.event;

public record ChatMessageBroadcastEvent(
        long outboxId
) {
}
