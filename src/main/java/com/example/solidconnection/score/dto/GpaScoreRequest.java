package com.example.solidconnection.score.dto;

import com.example.solidconnection.application.domain.Gpa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "대학 성적과 어학 시험 성적")
public record GpaScoreRequest(
        @NotNull(message = "학점을 입력해주세요.")
        @Schema(description = "GPA", example = "3.5", required = true)
        Double gpa,

        @NotNull(message = "학점 기준을 입력해주세요.")
        @Schema(description = "GPA 계산 기준", example = "4.0", required = true)
        Double gpaCriteria,

        @NotNull(message = "발급일자를 입력해주세요.")
        @Schema(description = "발급일자", example = "2024-10-06", required = true)
        LocalDate issueDate,

        @NotBlank(message = "대학 성적 증명서를 첨부해주세요.")
        @Schema(description = "대학 성적 증명서 URL", example = "http://example.com/gpa-report.pdf", required = true)
        String gpaReportUrl) {

    public Gpa toGpa() {
        return new Gpa(
                this.gpa,
                this.gpaCriteria,
                this.gpaReportUrl);
    }
}
