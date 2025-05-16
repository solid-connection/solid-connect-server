package com.example.solidconnection.score.dto;

import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.application.domain.VerifyStatus;

public record GpaScoreStatusResponse(
        long id,
        GpaResponse gpaResponse,
        VerifyStatus verifyStatus,
        String rejectedReason
) {
    public static GpaScoreStatusResponse from(GpaScore gpaScore) {
        return new GpaScoreStatusResponse(
                gpaScore.getId(),
                GpaResponse.from(gpaScore.getGpa()),
                gpaScore.getVerifyStatus(),
                gpaScore.getRejectedReason()
        );
    }
}
