package com.example.solidconnection.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import static com.example.solidconnection.constants.validMessage.FIRST_CHOICE_UNIVERSITY_ID_NOT_BLANK;

@Getter
@Setter
public class UniversityChoiceRequest {
    @NotNull(message = FIRST_CHOICE_UNIVERSITY_ID_NOT_BLANK)
    private Long firstChoiceUniversityId;
    private Long secondChoiceUniversityId;
}
