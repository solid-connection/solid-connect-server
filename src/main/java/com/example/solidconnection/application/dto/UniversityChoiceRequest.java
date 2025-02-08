package com.example.solidconnection.application.dto;

import com.example.solidconnection.custom.validation.annotation.ValidUniversityChoice;

@ValidUniversityChoice
public record UniversityChoiceRequest(
        Long firstChoiceUniversityId,
        Long secondChoiceUniversityId,
        Long thirdChoiceUniversityId) {
}
