package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.siteuser.domain.SiteUser;
import java.time.ZonedDateTime;

public record MentoringForMentorResponse(
        long mentoringId,
        String profileImageUrl,
        String nickname,
        boolean isChecked,
        VerifyStatus verifyStatus,
        ZonedDateTime createdAt
) {

    public static MentoringForMentorResponse of(Mentoring mentoring, SiteUser partner) {
        return new MentoringForMentorResponse(
                mentoring.getId(),
                partner.getProfileImageUrl(),
                partner.getNickname(),
                mentoring.getCheckedAtByMentor() != null,
                mentoring.getVerifyStatus(),
                mentoring.getCreatedAt()
        );
    }
}
