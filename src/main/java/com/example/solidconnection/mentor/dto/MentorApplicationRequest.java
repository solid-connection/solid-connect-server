package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.mentor.domain.UniversitySelectType;
import com.example.solidconnection.siteuser.domain.ExchangeStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MentorApplicationRequest(
        @NotNull(message = "교환 상태를 입력해주세요.")
        @JsonProperty("preparationStatus")
        ExchangeStatus exchangeStatus,

        @NotNull(message = "대학교 선택 유형을 입력해주세요.")
        UniversitySelectType universitySelectType,

        @NotNull(message = "국가를 입력해주세요")
        String country,

        Long universityId,

        @NotBlank(message = "학기를 입력해주세요.")
        String term
) {
}
