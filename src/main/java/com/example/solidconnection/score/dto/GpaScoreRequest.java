package com.example.solidconnection.score.dto;

import com.example.solidconnection.application.domain.Gpa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GpaScoreRequest(
        @NotNull(message = "학점을 입력해주세요.")
        Double gpa,

        @NotNull(message = "학점 기준을 입력해주세요.")
        Double gpaCriteria,

        @NotBlank(message = "대학 성적 증명서를 첨부해주세요.")
        String gpaReportUrl) {

    public Gpa toGpa() {
        return new Gpa(
                this.gpa,
                this.gpaCriteria,
                this.gpaReportUrl);
    }
}
