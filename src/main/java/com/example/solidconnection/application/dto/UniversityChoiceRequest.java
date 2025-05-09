package com.example.solidconnection.application.dto;

import com.example.solidconnection.university.dto.validation.ValidUniversityChoice;

@ValidUniversityChoice
public record UniversityChoiceRequest(
        Long firstChoiceUniversityId,
        Long secondChoiceUniversityId,
        Long thirdChoiceUniversityId) {
}
