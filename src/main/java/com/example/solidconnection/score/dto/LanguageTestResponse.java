package com.example.solidconnection.score.dto;

import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.university.domain.LanguageTestType;

public record LanguageTestResponse(
        LanguageTestType languageTestType,
        String languageTestScore,
        String languageTestReportUrl
) {
    public static LanguageTestResponse from(LanguageTest languageTest) {
        return new LanguageTestResponse(
                languageTest.getLanguageTestType(),
                languageTest.getLanguageTestScore(),
                languageTest.getLanguageTestReportUrl()
        );
    }
}
