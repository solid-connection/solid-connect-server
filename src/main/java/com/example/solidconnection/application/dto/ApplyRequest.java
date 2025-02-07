package com.example.solidconnection.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ApplyRequest(

        @NotNull(message = "gpa score id를 입력해주세요.")
        Long gpaScoreId,

        @NotNull(message = "language test score id를 입력해주세요.")
        Long languageTestScoreId,

        @Valid
        UniversityChoiceRequest universityChoiceRequest
) {
}
