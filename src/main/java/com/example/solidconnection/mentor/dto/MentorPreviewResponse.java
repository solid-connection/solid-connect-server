package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.siteuser.domain.ExchangeStatus;
import com.example.solidconnection.siteuser.domain.SiteUser;

import java.util.List;

public record MentorPreviewResponse(
        long id,
        String nickname,
        String profileImageUrl,
        ExchangeStatus exchangeStatus,
        String country,
        String universityName,
        int menteeCount,
        boolean hasBadge,
        String introduction,
        List<ChannelResponse> channels,
        boolean isApplied
) {

    public static MentorPreviewResponse of(Mentor mentor, SiteUser mentorUser, boolean isApplied) {
        return new MentorPreviewResponse(
                mentor.getId(),
                mentorUser.getNickname(),
                mentorUser.getProfileImageUrl(),
                mentorUser.getExchangeStatus(),
                "국가", // todo: 교환학생 기록이 인증되면 추가
                "대학 이름",  // todo: 교환학생 기록이 인증되면 추가
                mentor.getMenteeCount(),
                mentor.isHasBadge(),
                mentor.getIntroduction(),
                mentor.getChannels().stream().map(ChannelResponse::from).toList(),
                isApplied
        );
    }
}
