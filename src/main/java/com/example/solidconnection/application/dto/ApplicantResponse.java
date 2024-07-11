package com.example.solidconnection.application.dto;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.type.LanguageTestType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지원자")
public record ApplicantResponse(

        @Schema(description = "닉네임", example = "행복한 개발자")
        String nicknameForApply,

        @Schema(description = "GPA", example = "3.85")
        double gpa,

        @Schema(description = "어학 시험 유형", example = "TOEFL_IBT")
        LanguageTestType testType,

        @Schema(description = "어학 시험 점수", example = "110")
        String testScore,

        @Schema(description = "현재 사용자가 해당 지원지인지", example = "true")
        boolean isMine) {

    public static ApplicantResponse of(Application application, boolean isMine) {
        return new ApplicantResponse(
                application.getNicknameForApply(),
                application.getGpa().getGpa(),
                application.getLanguageTest().getLanguageTestType(),
                application.getLanguageTest().getLanguageTestScore(),
                isMine
        );
    }
}
