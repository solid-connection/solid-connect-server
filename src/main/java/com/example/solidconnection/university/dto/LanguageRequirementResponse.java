package com.example.solidconnection.university.dto;

import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.university.domain.LanguageRequirement;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "어학 성적 요구사항 응답")
public record LanguageRequirementResponse(
        @Schema(description = "어학 시험 유형", example = "TOEFL_IBT")
        LanguageTestType languageTestType,

        @Schema(description = "최소 점수 요구사항", example = "100")
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
