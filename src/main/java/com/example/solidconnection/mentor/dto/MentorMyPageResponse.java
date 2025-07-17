package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.siteuser.domain.ExchangeStatus;
import com.example.solidconnection.siteuser.domain.SiteUser;
import java.util.List;

public record MentorMyPageResponse(
        long id,
        String profileImageUrl,
        String nickname,
        ExchangeStatus exchangeStatus,
        String country,
        String universityName,
        int menteeCount,
        boolean hasBadge,
        String introduction,
        List<ChannelResponse> channels
) {

    public static MentorMyPageResponse of(Mentor mentor, SiteUser siteUser) {
        return new MentorMyPageResponse(
                mentor.getId(),
                siteUser.getProfileImageUrl(),
                siteUser.getNickname(),
                siteUser.getExchangeStatus(),
                "국가", // todo: 교환학생 기록이 인증되면 추가
                "대학 이름",
                mentor.getMenteeCount(),
                mentor.isHasBadge(),
                mentor.getIntroduction(),
                mentor.getChannels().stream()
                        .map(ChannelResponse::from)
                        .toList()
        );
    }
}
