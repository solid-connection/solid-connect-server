package com.example.solidconnection.admin.dto;

import com.example.solidconnection.university.domain.LanguageTestType;

public record LanguageTestResponse(
        LanguageTestType languageTestType,
        String languageTestScore,
        String languageTestReportUrl
) {
}
