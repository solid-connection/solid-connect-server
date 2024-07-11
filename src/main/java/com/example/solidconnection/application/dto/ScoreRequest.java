package com.example.solidconnection.application.dto;


import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.type.LanguageTestType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "대학 성적과 어학 시험 성적")
public record ScoreRequest(
        @NotNull(message = "어학 종류를 입력해주세요.")
        @Schema(description = "어학 시험 종류", example = "TOEFL", required = true)
        LanguageTestType languageTestType,

        @NotBlank(message = "어학 점수를 입력해주세요.")
        @Schema(description = "어학 시험 점수", example = "115", required = true)
        String languageTestScore,

        @NotBlank(message = "어학 증명서를 첨부해주세요.")
        @Schema(description = "어학 증명서 URL", example = "http://example.com/test-report.pdf", required = true)
        String languageTestReportUrl,

        @NotNull(message = "학점을 입력해주세요.")
        @Schema(description = "GPA", example = "3.5", required = true)
        Double gpa,

        @NotNull(message = "학점 기준을 입력해주세요.")
        @Schema(description = "GPA 계산 기준", example = "4.0", required = true)
        Double gpaCriteria,

        @NotBlank(message = "대학 성적 증명서를 첨부해주세요.")
        @Schema(description = "대학 성적 증명서 URL", example = "http://example.com/gpa-report.pdf", required = true)
        String gpaReportUrl) {

    public Gpa toGpa() {
        return new Gpa(
                this.gpa,
                this.gpaCriteria,
                this.gpaReportUrl);
    }

    public LanguageTest toLanguageTest() {
        return new LanguageTest(
                this.languageTestType,
                this.languageTestScore,
                this.languageTestReportUrl
        );
    }
}
