package com.example.solidconnection.application.dto;

import com.example.solidconnection.university.domain.UnivApplyInfo;

public record ApplicationUniversityPreviewResponse(
        long id,
        String koreanName,
        Integer studentCapacity,
        String region,
        String country,
        String logoImageUrl,
        String backgroundImageUrl) {

    public static ApplicationUniversityPreviewResponse from(UnivApplyInfo univApplyInfo) {
        return new ApplicationUniversityPreviewResponse(
                univApplyInfo.getId(),
                univApplyInfo.getKoreanName(),
                univApplyInfo.getStudentCapacity(),
                univApplyInfo.getUniversity().getRegion().getKoreanName(),
                univApplyInfo.getUniversity().getCountry().getKoreanName(),
                univApplyInfo.getUniversity().getLogoImageUrl(),
                univApplyInfo.getUniversity().getBackgroundImageUrl()
        );
    }
}
