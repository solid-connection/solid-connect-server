package com.example.solidconnection.application.dto;

import com.example.solidconnection.custom.validation.annotation.ValidUniversityChoice;
import jakarta.validation.constraints.NotNull;

@ValidUniversityChoice
public record UniversityChoiceRequest(
        @NotNull(message = "1지망 대학교를 입력해주세요.")
        Long firstChoiceUniversityId,
        Long secondChoiceUniversityId,
        Long thirdChoiceUniversityId) {
}
