package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.mentor.domain.Mentoring;

public record MentoringConfirmResponse(
        Long mentoringId
) {
    public static MentoringConfirmResponse from(Mentoring mentoring) {
        return new MentoringConfirmResponse(mentoring.getId());
    }
}
