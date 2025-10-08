package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.University;
import java.util.List;

public record MentorMyPageResponse(
        long id,
        String profileImageUrl,
        String nickname,
        String country,
        String universityName,
        String term,
        int menteeCount,
        boolean hasBadge,
        String introduction,
        String passTip,
        List<ChannelResponse> channels
) {

    public static MentorMyPageResponse of(Mentor mentor, SiteUser siteUser, University university, String termName) {
        return new MentorMyPageResponse(
                mentor.getId(),
                siteUser.getProfileImageUrl(),
                siteUser.getNickname(),
                university.getCountry().getKoreanName(),
                university.getKoreanName(),
                termName,
                mentor.getMenteeCount(),
                mentor.isHasBadge(),
                mentor.getIntroduction(),
                mentor.getPassTip(),
                mentor.getChannels().stream()
                        .map(ChannelResponse::from)
                        .toList()
        );
    }
}
