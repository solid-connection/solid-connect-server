package com.example.solidconnection.university.dto;

import com.example.solidconnection.university.domain.UnivApplyInfo;

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

    public static UniversityInfoForApplyPreviewResponse from(UnivApplyInfo univApplyInfo) {
        List<LanguageRequirementResponse> languageRequirementResponses = new java.util.ArrayList<>(
                univApplyInfo.getLanguageRequirements().stream()
                        .map(LanguageRequirementResponse::from)
                        .toList());
        Collections.sort(languageRequirementResponses);

        return new UniversityInfoForApplyPreviewResponse(
                univApplyInfo.getId(),
                univApplyInfo.getTerm(),
                univApplyInfo.getKoreanName(),
                univApplyInfo.getUniversity().getRegion().getKoreanName(),
                univApplyInfo.getUniversity().getCountry().getKoreanName(),
                univApplyInfo.getUniversity().getLogoImageUrl(),
                univApplyInfo.getUniversity().getBackgroundImageUrl(),
                univApplyInfo.getStudentCapacity(),
                languageRequirementResponses
        );
    }
}
