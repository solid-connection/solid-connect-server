package com.example.solidconnection.admin.dto;

import com.example.solidconnection.custom.validation.annotation.RejectedReasonRequired;
import com.example.solidconnection.type.VerifyStatus;
import jakarta.validation.constraints.NotNull;

@RejectedReasonRequired
public record GpaScoreVerifyRequest(
        @NotNull(message = "승인 여부를 설정해주세요.")
        VerifyStatus verifyStatus,

        String rejectedReason
) {
}
