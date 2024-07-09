package com.example.solidconnection.university.dto;

import com.example.solidconnection.university.domain.UniversityInfoForApply;

import java.util.List;

public record UniversityInfoForApplyPreviewResponse(
        long id,
        String term,
        String koreanName,
        String region,
        String country,
        String logoImageUrl,
        int studentCapacity,
        List<LanguageRequirementResponse> languageRequirements) {

    public static UniversityInfoForApplyPreviewResponse from(UniversityInfoForApply universityInfoForApply) {
        return new UniversityInfoForApplyPreviewResponse(
                universityInfoForApply.getId(),
                universityInfoForApply.getTerm(),
                universityInfoForApply.getUniversity().getKoreanName(),
                universityInfoForApply.getUniversity().getRegion().getKoreanName(),
                universityInfoForApply.getUniversity().getCountry().getKoreanName(),
                universityInfoForApply.getUniversity().getLogoImageUrl(),
                universityInfoForApply.getStudentCapacity(),
                universityInfoForApply.getLanguageRequirements().stream()
                        .map(LanguageRequirementResponse::from)
                        .toList()
        );
    }
}
