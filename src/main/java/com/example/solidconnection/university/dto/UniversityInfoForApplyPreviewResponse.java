package com.example.solidconnection.university.dto;

import com.example.solidconnection.university.domain.UniversityInfoForApply;

import java.util.Collections;
import java.util.List;

public record UniversityInfoForApplyPreviewResponse(
        long id,
        String term,
        String koreanName,
        String region,
        String country,
        String logoImageUrl,
        String backgroundImageUrl,
        int studentCapacity,
        List<LanguageRequirementResponse> languageRequirements) {

    public static UniversityInfoForApplyPreviewResponse from(UniversityInfoForApply universityInfoForApply) {
        List<LanguageRequirementResponse> languageRequirementResponses = new java.util.ArrayList<>(
                universityInfoForApply.getLanguageRequirements().stream()
                        .map(LanguageRequirementResponse::from)
                        .toList());
        Collections.sort(languageRequirementResponses);

        return new UniversityInfoForApplyPreviewResponse(
                universityInfoForApply.getId(),
                universityInfoForApply.getTerm(),
                universityInfoForApply.getKoreanName(),
                universityInfoForApply.getUniversity().getRegion().getKoreanName(),
                universityInfoForApply.getUniversity().getCountry().getKoreanName(),
                universityInfoForApply.getUniversity().getLogoImageUrl(),
                universityInfoForApply.getUniversity().getBackgroundImageUrl(),
                universityInfoForApply.getStudentCapacity(),
                languageRequirementResponses
        );
    }
}
