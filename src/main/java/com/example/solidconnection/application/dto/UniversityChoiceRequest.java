package com.example.solidconnection.application.dto;

import jakarta.validation.constraints.NotNull;

import static com.example.solidconnection.constants.validMessage.FIRST_CHOICE_UNIVERSITY_ID_NOT_BLANK;

public record UniversityChoiceRequest(
        @NotNull(message = FIRST_CHOICE_UNIVERSITY_ID_NOT_BLANK)
        Long firstChoiceUniversityId,
        Long secondChoiceUniversityId) {
}
