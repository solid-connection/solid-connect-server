package com.example.solidconnection.score.dto;

import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.score.domain.LanguageTestScore;

public record LanguageTestScoreStatusResponse(
        long id,
        LanguageTestResponse languageTestResponse,
        VerifyStatus verifyStatus,
        String rejectedReason
) {

    public static LanguageTestScoreStatusResponse from(LanguageTestScore languageTestScore) {
        return new LanguageTestScoreStatusResponse(
                languageTestScore.getId(),
                LanguageTestResponse.from(languageTestScore.getLanguageTest()),
                languageTestScore.getVerifyStatus(),
                languageTestScore.getRejectedReason()
        );
    }
}
