package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.siteuser.domain.SiteUser;
import java.time.ZonedDateTime;

public record MentoringForMenteeResponse(
        long mentoringId,
        String profileImageUrl,
        String nickname,
        boolean isChecked,
        ZonedDateTime createdAt,
        Long chatRoomId
) {

    public static MentoringForMenteeResponse of(Mentoring mentoring, SiteUser partner, Long chatRoomId) {
        return new MentoringForMenteeResponse(
                mentoring.getId(),
                partner.getProfileImageUrl(),
                partner.getNickname(),
                mentoring.getCheckedAtByMentee() != null,
                mentoring.getCreatedAt(),
                chatRoomId
        );
    }
}
