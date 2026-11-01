package com.example.solidconnection.chat.event;

import com.example.solidconnection.chat.dto.ChatMessageSendResponse;

public record ChatMessageBroadcastEvent(
        long roomId,
        ChatMessageSendResponse response
) {

}
