package com.example.solidconnection.university.dto;

import com.example.solidconnection.university.domain.UnivApplyInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record UnivApplyInfoPreviewResponse(
        long id,
        String term,
        String koreanName,
        String region,
        String country,
        String logoImageUrl,
        String backgroundImageUrl,
        int studentCapacity,
        List<LanguageRequirementResponse> languageRequirements) {

    public static UnivApplyInfoPreviewResponse from(UnivApplyInfo univApplyInfo, String termName) {
        List<LanguageRequirementResponse> languageRequirementResponses = new ArrayList<>(
                univApplyInfo.getLanguageRequirements().stream()
                        .map(LanguageRequirementResponse::from)
                        .toList());
        Collections.sort(languageRequirementResponses);

        return new UnivApplyInfoPreviewResponse(
                univApplyInfo.getId(),
                termName,
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
