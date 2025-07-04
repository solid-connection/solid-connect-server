package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.siteuser.domain.SiteUser;

import java.time.ZonedDateTime;

public record MentoringResponse(
        long mentoringId,
        String profileImageUrl,
        String nickname,
        boolean isChecked,
        ZonedDateTime createAt
) {
    public static MentoringResponse from(Mentoring mentoring, SiteUser mentee) {
        return new MentoringResponse(
                mentoring.getId(),
                mentee.getProfileImageUrl(),
                mentee.getNickname(),
                mentoring.getCheckedAt() != null,
                mentoring.getCreatedAt()
        );
    }
}
