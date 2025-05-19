package com.example.solidconnection.university.dto;

import com.example.solidconnection.university.domain.LanguageRequirement;
import com.example.solidconnection.university.domain.LanguageTestType;

public record LanguageRequirementResponse(
        LanguageTestType languageTestType,
        String minScore) implements Comparable<LanguageRequirementResponse> {

    public static LanguageRequirementResponse from(LanguageRequirement languageRequirement) {
        return new LanguageRequirementResponse(
                languageRequirement.getLanguageTestType(),
                languageRequirement.getMinScore());
    }

    @Override
    public int compareTo(LanguageRequirementResponse other) {
        return this.languageTestType.name().compareTo(other.languageTestType.name());
    }
}
