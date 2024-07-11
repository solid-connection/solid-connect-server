package com.example.solidconnection.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "지망 대학")
public record UniversityChoiceRequest(

        @NotNull(message = "1지망 대학교를 입력해주세요.")
        @Schema(description = "1지망 대학교의 지원 정보 ID", example = "1")
        Long firstChoiceUniversityId,

        @Schema(description = "2지망 대학교의 지원 정보 ID (선택사항)", example = "2", nullable = true)
        Long secondChoiceUniversityId) {
}
