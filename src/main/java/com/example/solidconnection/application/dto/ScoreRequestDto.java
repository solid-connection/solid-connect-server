package com.example.solidconnection.application.dto;


import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.type.LanguageTestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import static com.example.solidconnection.constants.validMessage.GPA_CRITERIA_NOT_BLANK;
import static com.example.solidconnection.constants.validMessage.GPA_NOT_BLANK;
import static com.example.solidconnection.constants.validMessage.GPA_REPORT_URL_NOT_BLANK;
import static com.example.solidconnection.constants.validMessage.LANGUAGE_TEST_REPORT_URL_NOT_BLANK;
import static com.example.solidconnection.constants.validMessage.LANGUAGE_TEST_SCORE_NOT_BLANK;
import static com.example.solidconnection.constants.validMessage.LANGUAGE_TEST_TYPE_NOT_BLANK;

@Getter
public class ScoreRequestDto {

    @NotNull(message = LANGUAGE_TEST_TYPE_NOT_BLANK)
    private LanguageTestType languageTestType;

    @NotBlank(message = LANGUAGE_TEST_SCORE_NOT_BLANK)
    private String languageTestScore;

    @NotBlank(message = LANGUAGE_TEST_REPORT_URL_NOT_BLANK)
    private String languageTestReportUrl;

    @NotNull(message = GPA_NOT_BLANK)
    private Float gpa;

    @NotNull(message = GPA_CRITERIA_NOT_BLANK)
    private Float gpaCriteria;

    @NotBlank(message = GPA_REPORT_URL_NOT_BLANK)
    private String gpaReportUrl;

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
