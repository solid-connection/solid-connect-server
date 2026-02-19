package com.example.solidconnection.chat.dto;

public record ChatParticipantResponse(
        long siteUserId,
        String nickname,
        String profileUrl
) {

    public static ChatParticipantResponse of(long siteUserId, String nickname, String profileUrl) {
        return new ChatParticipantResponse(siteUserId, nickname, profileUrl);
    }
}
