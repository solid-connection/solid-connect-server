package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.mentor.domain.Mentoring;

public record MentoringApplyResponse(
        long mentoringId
) {

    public static MentoringApplyResponse from(Mentoring mentoring) {
        return new MentoringApplyResponse(mentoring.getId());
    }
}
