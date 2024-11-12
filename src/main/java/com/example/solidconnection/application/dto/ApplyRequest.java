package com.example.solidconnection.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "지원서 제출")
public record ApplyRequest(
        @NotNull(message = "gpa score id를 입력해주세요.")
        @Schema(description = "지원하는 유저의 gpa score id", example = "1")
        Long gpaScoreId,

        @NotNull(message = "language test score id를 입력해주세요.")
        @Schema(description = "지원하는 유저의 language test score id", example = "1")
        Long languageTestScoreId,

        UniversityChoiceRequest universityChoiceRequest
) {
}
