package com.example.solidconnection.application.dto;

import com.example.solidconnection.common.validation.annotation.ValidUniversityChoice;

@ValidUniversityChoice
public record UniversityChoiceRequest(
        Long firstChoiceUniversityId,
        Long secondChoiceUniversityId,
        Long thirdChoiceUniversityId) {
}
