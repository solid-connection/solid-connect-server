package com.example.solidconnection.score.dto;

import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.type.VerifyStatus;

public record LanguageTestScoreStatus(
        Long id,
        LanguageTest languageTest,
        VerifyStatus verifyStatus,
        String rejectedReason
) {
    public static LanguageTestScoreStatus from(LanguageTestScore languageTestScore) {
        return new LanguageTestScoreStatus(
                languageTestScore.getId(),
                languageTestScore.getLanguageTest(),
                languageTestScore.getVerifyStatus(),
                languageTestScore.getRejectedReason()
        );
    }
}
