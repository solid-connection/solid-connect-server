package com.example.solidconnection.chat.dto;

public record ChatParticipantResponse(
        long partnerId, // siteUserId
        String nickname,
        String profileUrl
) {

    public static ChatParticipantResponse of(long partnerId, String nickname, String profileUrl) {
        return new ChatParticipantResponse(partnerId, nickname, profileUrl);
    }
}
