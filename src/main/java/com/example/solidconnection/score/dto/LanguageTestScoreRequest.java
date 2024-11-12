package com.example.solidconnection.score.dto;


import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.type.LanguageTestType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "대학 성적과 어학 시험 성적")
public record LanguageTestScoreRequest(
        @NotNull(message = "어학 종류를 입력해주세요.")
        @Schema(description = "어학 시험 종류", example = "TOEFL", required = true)
        LanguageTestType languageTestType,

        @NotBlank(message = "어학 점수를 입력해주세요.")
        @Schema(description = "어학 시험 점수", example = "115", required = true)
        String languageTestScore,

        @NotNull(message = "발급일자를 입력해주세요.")
        @Schema(description = "발급일자", example = "2024-10-06", required = true)
        LocalDate issueDate,

        @NotBlank(message = "어학 증명서를 첨부해주세요.")
        @Schema(description = "어학 증명서 URL", example = "http://example.com/test-report.pdf", required = true)
        String languageTestReportUrl) {

    public LanguageTest toLanguageTest() {
        return new LanguageTest(
                this.languageTestType,
                this.languageTestScore,
                this.languageTestReportUrl
        );
    }
}
