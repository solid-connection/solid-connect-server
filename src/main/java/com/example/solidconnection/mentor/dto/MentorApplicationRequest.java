package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.mentor.domain.UniversitySelectType;
import com.example.solidconnection.siteuser.domain.ExchangeStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MentorApplicationRequest(
        @JsonProperty("preparationStatus")
        ExchangeStatus exchangeStatus,
        UniversitySelectType universitySelectType,
        String country,
        Long universityId
) {
}
