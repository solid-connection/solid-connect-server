package com.example.solidconnection.university.dto;

import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.university.domain.LanguageRequirement;

public record LanguageRequirementResponse(
        LanguageTestType languageTestType,
        String minScore) {

    public static LanguageRequirementResponse from(LanguageRequirement languageRequirement) {
        return new LanguageRequirementResponse(
                languageRequirement.getLanguageTestType(),
                languageRequirement.getMinScore());
    }
}
