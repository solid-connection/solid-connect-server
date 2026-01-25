package com.example.solidconnection.university.dto;

import com.example.solidconnection.university.domain.UnivApplyInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record UnivApplyInfoPreviewResponse(
        long id,
        String term,
        String koreanName,
        String homeUniversityName,
        String region,
        String country,
        String logoImageUrl,
        String backgroundImageUrl,
        int studentCapacity,
        List<LanguageRequirementResponse> languageRequirements) {

    public static UnivApplyInfoPreviewResponse of(UnivApplyInfo univApplyInfo, String termName) {
        List<LanguageRequirementResponse> languageRequirementResponses = new ArrayList<>(
                univApplyInfo.getLanguageRequirements().stream()
                        .map(LanguageRequirementResponse::from)
                        .toList());
        Collections.sort(languageRequirementResponses);

        String homeUniversityName = univApplyInfo.getHomeUniversity() != null
                ? univApplyInfo.getHomeUniversity().getName()
                : null;

        return new UnivApplyInfoPreviewResponse(
                univApplyInfo.getId(),
                termName,
                univApplyInfo.getKoreanName(),
                homeUniversityName,
                univApplyInfo.getUniversity().getRegion().getKoreanName(),
                univApplyInfo.getUniversity().getCountry().getKoreanName(),
                univApplyInfo.getUniversity().getLogoImageUrl(),
                univApplyInfo.getUniversity().getBackgroundImageUrl(),
                univApplyInfo.getStudentCapacity(),
                languageRequirementResponses
        );
    }
}
