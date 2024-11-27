package com.example.solidconnection.score.dto;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.type.VerifyStatus;

import java.time.LocalDate;

public record GpaScoreStatus(
        Long id,
        Gpa gpa,
        LocalDate issueDate,
        VerifyStatus verifyStatus,
        String rejectedReason
) {
    public static GpaScoreStatus from(GpaScore gpaScore) {
        return new GpaScoreStatus(
                gpaScore.getId(),
                gpaScore.getGpa(),
                gpaScore.getIssueDate(),
                gpaScore.getVerifyStatus(),
                gpaScore.getRejectedReason()
        );
    }
}
