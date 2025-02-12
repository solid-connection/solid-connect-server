package com.example.solidconnection.score.dto;

import com.example.solidconnection.type.LanguageTestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LanguageTestScoreRequest(
        @NotNull(message = "어학 종류를 입력해주세요.")
        LanguageTestType languageTestType,

        @NotBlank(message = "어학 점수를 입력해주세요.")
        String languageTestScore
) {
}
