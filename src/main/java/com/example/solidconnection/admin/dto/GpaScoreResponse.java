package com.example.solidconnection.admin.dto;

import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.score.domain.GpaScore;

public record GpaScoreResponse(
        long id,
        double gpa,
        double gpaCriteria,
        VerifyStatus verifyStatus,
        String rejectedReason
) {
    public static GpaScoreResponse from(GpaScore gpaScore) {
        return new GpaScoreResponse(
                gpaScore.getId(),
                gpaScore.getGpa().getGpa(),
                gpaScore.getGpa().getGpaCriteria(),
                gpaScore.getVerifyStatus(),
                gpaScore.getRejectedReason()
        );
    }
}
