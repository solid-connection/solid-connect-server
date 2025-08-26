package com.example.solidconnection.application.dto;

import com.example.solidconnection.university.dto.validation.ValidUnivApplyInfoChoice;
import com.fasterxml.jackson.annotation.JsonProperty;

@ValidUnivApplyInfoChoice
public record UnivApplyInfoChoiceRequest(

        @JsonProperty("firstChoiceUniversityId")
        Long firstChoiceUnivApplyInfoId,

        @JsonProperty("secondChoiceUniversityId")
        Long secondChoiceUnivApplyInfoId,

        @JsonProperty("thirdChoiceUniversityId")
        Long thirdChoiceUnivApplyInfoId) {

}
