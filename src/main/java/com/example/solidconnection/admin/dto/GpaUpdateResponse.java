package com.example.solidconnection.admin.dto;

import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.type.VerifyStatus;

public record GpaUpdateResponse(
        Long id,
        Double gpa,
        Double gpaCriteria,
        VerifyStatus verifyStatus
) {
    public static GpaUpdateResponse of(GpaScore gpaScore) {
        return new GpaUpdateResponse(
                gpaScore.getId(),
                gpaScore.getGpa().getGpa(),
                gpaScore.getGpa().getGpaCriteria(),
                gpaScore.getVerifyStatus()
        );
    }
}
