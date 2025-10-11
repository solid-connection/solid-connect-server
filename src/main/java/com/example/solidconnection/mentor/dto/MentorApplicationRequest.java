package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.mentor.domain.ExchangePhase;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MentorApplicationRequest(
        @JsonProperty("preparationStatus")
        ExchangePhase exchangePhase,
        String country,
        String region,
        Long universityId
) {
}
