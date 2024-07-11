package com.example.solidconnection.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지원 정보 제출 성공 여부")
public record ApplicationSubmissionResponse(

        @Schema(description = "제출 성공 여부", example = "true")
        boolean isSuccess) {
}
