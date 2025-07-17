package com.example.solidconnection.score.dto;

import java.util.List;

public record LanguageTestScoreStatusesResponse(
        List<LanguageTestScoreStatusResponse> languageTestScoreStatusResponseList
) {

    public static LanguageTestScoreStatusesResponse from(List<LanguageTestScoreStatusResponse> languageTestScoreStatusResponseList) {
        return new LanguageTestScoreStatusesResponse(languageTestScoreStatusResponseList);
    }
}
