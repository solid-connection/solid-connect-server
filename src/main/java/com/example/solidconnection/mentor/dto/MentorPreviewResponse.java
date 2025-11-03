package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.University;
import java.util.List;

public record MentorPreviewResponse(
        long id,
        String nickname,
        String profileImageUrl,
        String country,
        String universityName,
        String term,
        int menteeCount,
        boolean hasBadge,
        String introduction,
        List<ChannelResponse> channels,
        boolean isApplied
) {

    public static MentorPreviewResponse of(Mentor mentor, SiteUser mentorUser,
                                           University university, boolean isApplied,
                                           String termName
    ) {
        return new MentorPreviewResponse(
                mentor.getId(),
                mentorUser.getNickname(),
                mentorUser.getProfileImageUrl(),
                university.getCountry().getKoreanName(),
                university.getKoreanName(),
                termName,
                mentor.getMenteeCount(),
                mentor.isHasBadge(),
                mentor.getIntroduction(),
                mentor.getChannels().stream().map(ChannelResponse::from).toList(),
                isApplied
        );
    }
}
