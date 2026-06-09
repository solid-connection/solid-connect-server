package com.example.solidconnection.application.dto;

import com.example.solidconnection.university.dto.validation.ValidUnivApplyInfoChoice;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@ValidUnivApplyInfoChoice
public record UnivApplyInfoChoiceRequest(

        @JsonProperty("choices")
        List<Long> univApplyInfoIds) {

}
