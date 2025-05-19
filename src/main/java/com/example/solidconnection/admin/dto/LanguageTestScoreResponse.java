package com.example.solidconnection.admin.dto;

import com.example.solidconnection.application.domain.VerifyStatus;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.university.domain.LanguageTestType;

public record LanguageTestScoreResponse(
        long id,
        LanguageTestType languageTestType,
        String languageTestScore,
        VerifyStatus verifyStatus,
        String rejectedReason
) {
    public static LanguageTestScoreResponse from(LanguageTestScore languageTestScore) {
        return new LanguageTestScoreResponse(
                languageTestScore.getId(),
                languageTestScore.getLanguageTest().getLanguageTestType(),
                languageTestScore.getLanguageTest().getLanguageTestScore(),
                languageTestScore.getVerifyStatus(),
                languageTestScore.getRejectedReason()
        );
    }
}
