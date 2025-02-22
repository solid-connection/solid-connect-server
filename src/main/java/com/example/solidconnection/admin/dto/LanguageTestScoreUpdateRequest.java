package com.example.solidconnection.admin.dto;

import com.example.solidconnection.custom.validation.annotation.RejectedReasonRequired;
import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.type.VerifyStatus;
import jakarta.validation.constraints.NotNull;

@RejectedReasonRequired
public record LanguageTestScoreUpdateRequest(

        @NotNull(message = "어학 유형을 입력해주세요.")
        LanguageTestType languageTestType,

        @NotNull(message = "어학 점수를 입력해주세요.")
        String languageTestScore,

        @NotNull(message = "승인 상태를 설정해주세요.")
        VerifyStatus verifyStatus,

        String rejectedReason
) implements ScoreUpdateRequest {
}
