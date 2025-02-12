package com.example.solidconnection.score.dto;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.type.VerifyStatus;

public record GpaScoreStatus(
        Long id,
        Gpa gpa,
        VerifyStatus verifyStatus,
        String rejectedReason
) {
    public static GpaScoreStatus from(GpaScore gpaScore) {
        return new GpaScoreStatus(
                gpaScore.getId(),
                gpaScore.getGpa(),
                gpaScore.getVerifyStatus(),
                gpaScore.getRejectedReason()
        );
    }
}
