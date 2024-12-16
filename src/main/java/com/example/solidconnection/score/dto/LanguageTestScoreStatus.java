package com.example.solidconnection.score.dto;

import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.type.VerifyStatus;

import java.time.LocalDate;

public record LanguageTestScoreStatus(
        Long id,
        LanguageTest languageTest,
        LocalDate issueDate,
        VerifyStatus verifyStatus,
        String rejectedReason
) {
    public static LanguageTestScoreStatus from(LanguageTestScore languageTestScore) {
        return new LanguageTestScoreStatus(
                languageTestScore.getId(),
                languageTestScore.getLanguageTest(),
                languageTestScore.getIssueDate(),
                languageTestScore.getVerifyStatus(),
                languageTestScore.getRejectedReason()
        );
    }
}
