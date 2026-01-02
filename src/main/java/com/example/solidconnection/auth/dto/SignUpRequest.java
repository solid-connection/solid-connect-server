package com.example.solidconnection.auth.dto;

import com.example.solidconnection.siteuser.domain.ExchangeStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record SignUpRequest(
        String signUpToken,
        List<String> interestedRegions,
        List<String> interestedCountries,

        @JsonProperty("preparationStatus")
        ExchangeStatus exchangeStatus,

        String profileImageUrl,

        @NotBlank(message = "닉네임을 입력해주세요.")
        String nickname) {
}
