package com.example.solidconnection.score.dto;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.type.VerifyStatus;

import java.time.LocalDate;

public record GpaScoreStatusResponse(
        Long id,
        Gpa gpa,
        LocalDate issueDate,
        VerifyStatus verifyStatus,
        String rejectedReason
) {
    public static GpaScoreStatusResponse from(GpaScore gpaScore) {
        return new GpaScoreStatusResponse(
                gpaScore.getId(),
                gpaScore.getGpa(),
                gpaScore.getIssueDate(),
                gpaScore.getVerifyStatus(),
                gpaScore.getRejectedReason()
        );
    }
}
