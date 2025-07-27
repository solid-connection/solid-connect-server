package com.example.solidconnection.chat.dto;

import java.time.ZonedDateTime;

public record ChatRoomResponse(
        long id,
        String lastChatMessage,
        ZonedDateTime lastReceivedTime,
        ChatParticipantResponse partner,
        long unReadCount
) {

    public static ChatRoomResponse of(
            long id,
            String lastChatMessage,
            ZonedDateTime lastReceivedTime,
            ChatParticipantResponse partner,
            long unReadCount
    ) {
        return new ChatRoomResponse(id, lastChatMessage, lastReceivedTime, partner, unReadCount);
    }
}
