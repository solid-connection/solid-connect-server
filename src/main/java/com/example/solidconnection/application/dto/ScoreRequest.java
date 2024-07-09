package com.example.solidconnection.application.dto;


import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.type.LanguageTestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScoreRequest(
        @NotNull(message = "어학 종류를 입력해주세요.")
        LanguageTestType languageTestType,
        @NotBlank(message = "어학 점수를 입력해주세요.")
        String languageTestScore,
        @NotBlank(message = "어학 증명서를 첨부해주세요.")
        String languageTestReportUrl,
        @NotNull(message = "학점을 입력해주세요.")
        Float gpa,
        @NotNull(message = "학점 기준을 입력해주세요.")
        Float gpaCriteria,
        @NotBlank(message = "대학 성적 증명서를 첨부해주세요.")
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
