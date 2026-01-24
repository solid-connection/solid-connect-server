package com.example.solidconnection.mentor.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.HostUniversity;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public record MatchedMentorResponse(
        long id,

        @JsonInclude(NON_NULL)
        Long roomId,

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

    public static MatchedMentorResponse of(Mentor mentor, SiteUser mentorUser,
                                           HostUniversity university, boolean isApplied, Long roomId,
                                           String termName
    ) {
        return new MatchedMentorResponse(
                mentor.getId(),
                roomId,
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
