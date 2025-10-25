package com.example.solidconnection.chat.dto;

public record ChatParticipantResponse(
        long partnerId, // 멘티는 siteUserId, 멘토는 mentorId
        String nickname,
        String profileUrl
) {

    public static ChatParticipantResponse of(long partnerId, String nickname, String profileUrl) {
        return new ChatParticipantResponse(partnerId, nickname, profileUrl);
    }
}
