package com.example.solidconnection.admin.dto;

import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.type.VerifyStatus;

public record GpaScoreVerificationResponse(
        Long id,
        VerifyStatus verifyStatus
) {
    public static GpaScoreVerificationResponse of(GpaScore gpaScore) {
        return new GpaScoreVerificationResponse(
                gpaScore.getId(),
                gpaScore.getVerifyStatus());
    }
}
