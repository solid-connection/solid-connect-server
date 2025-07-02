package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.mentor.domain.Mentoring;

import java.time.ZonedDateTime;

public record MentoringResponse(
        Long id,
        Long mentorId,
        Long menteeId,
        ZonedDateTime createdAt,
        ZonedDateTime confirmedAt,
        VerifyStatus verifyStatus,
        String rejectedReason
) {
    public static MentoringResponse from(Mentoring mentoring) {
        return new MentoringResponse(
                mentoring.getId(),
                mentoring.getMentorId(),
                mentoring.getMenteeId(),
                mentoring.getCreatedAt(),
                mentoring.getConfirmedAt(),
                mentoring.getVerifyStatus(),
                mentoring.getRejectedReason()
        );
    }
}
