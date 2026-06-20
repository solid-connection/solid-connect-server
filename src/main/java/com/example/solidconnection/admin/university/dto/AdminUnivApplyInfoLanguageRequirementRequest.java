package com.example.solidconnection.admin.university.dto;

import com.example.solidconnection.university.domain.LanguageTestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminUnivApplyInfoLanguageRequirementRequest(
        @NotNull LanguageTestType languageTestType,
        @NotBlank String minScore
) {
}
