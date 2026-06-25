package com.example.solidconnection.university.dto;

import com.example.solidconnection.university.domain.HostUniversity;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import java.util.List;
import java.util.Map;

public record UnivApplyInfoDetailResponse(
        long id,
        String term,
        String koreanName,
        String englishName,
        String formatName,
        String region,
        String country,
        String homepageUrl,
        String logoImageUrl,
        String backgroundImageUrl,
        String detailsForLocal,
        Integer studentCapacity,
        String semesterAvailableForDispatch,
        List<LanguageRequirementResponse> languageRequirements,
        String detailsForLanguage,
        String gpaRequirement,
        String gpaRequirementCriteria,
        String semesterRequirement,
        String detailsForAccommodation,
        String accommodationUrl,
        String englishCourseUrl,
        Map<String, String> extraInfo) {

    public static UnivApplyInfoDetailResponse of(
            HostUniversity university,
            UnivApplyInfo univApplyInfo,
            String termName
    ) {
        return new UnivApplyInfoDetailResponse(
                univApplyInfo.getId(),
                termName,
                univApplyInfo.getKoreanName(),
                university.getEnglishName(),
                university.getFormatName(),
                university.getRegion().getKoreanName(),
                university.getCountry().getKoreanName(),
                university.getHomepageUrl(),
                university.getLogoImageUrl(),
                university.getBackgroundImageUrl(),
                university.getDetailsForLocal(),
                univApplyInfo.getStudentCapacity(),
                univApplyInfo.getSemesterAvailableForDispatch().getKoreanName(),
                univApplyInfo.getLanguageRequirements().stream()
                        .map(LanguageRequirementResponse::from)
                        .toList(),
                univApplyInfo.getDetailsForLanguage(),
                univApplyInfo.getGpaRequirement(),
                univApplyInfo.getGpaRequirementCriteria(),
                univApplyInfo.getSemesterRequirement(),
                univApplyInfo.getDetailsForAccommodation(),
                university.getAccommodationUrl(),
                university.getEnglishCourseUrl(),
                univApplyInfo.getExtraInfo()
        );
    }
}
