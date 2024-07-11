package com.example.solidconnection.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지원 상태와 지망 대학 변경 횟수")
public record VerifyStatusResponse(

        @Schema(description = "지원 상태", example = "SUBMITTED_PENDING")
        String status,

        @Schema(description = "지망 대학 변경 횟수", example = "1")
        int updateCount) {
}
