package com.example.solidconnection.application.dto;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.type.LanguageTestType;

public record ApplicantResponse(
        String nicknameForApply,
        double gpa,
        LanguageTestType testType,
        String testScore,
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
