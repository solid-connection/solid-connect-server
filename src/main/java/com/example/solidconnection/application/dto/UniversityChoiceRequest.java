package com.example.solidconnection.application.dto;

import jakarta.validation.constraints.NotNull;

public record UniversityChoiceRequest(
        @NotNull(message = "1지망 대학교를 입력해주세요.")
        Long firstChoiceUniversityId,

        Long secondChoiceUniversityId,
        Long thirdChoiceUniversityId) {
}
