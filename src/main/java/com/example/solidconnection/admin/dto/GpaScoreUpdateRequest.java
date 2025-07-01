package com.example.solidconnection.admin.dto;

import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.application.dto.validation.RejectedReasonRequired;
import jakarta.validation.constraints.NotNull;

@RejectedReasonRequired
public record GpaScoreUpdateRequest(

        @NotNull(message = "GPA를 입력해주세요.")
        Double gpa,

        @NotNull(message = "GPA 기준을 입력해주세요.")
        Double gpaCriteria,

        @NotNull(message = "승인 상태를 설정해주세요.")
        VerifyStatus verifyStatus,

        String rejectedReason
) implements ScoreUpdateRequest {
}
