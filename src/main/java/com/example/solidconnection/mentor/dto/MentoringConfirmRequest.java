package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.common.VerifyStatus;
import jakarta.validation.constraints.NotNull;

public record MentoringConfirmRequest(

        @NotNull(message = "승인 상태를 설정해주세요.")
        VerifyStatus status,
        String rejectedReason
) {
}
