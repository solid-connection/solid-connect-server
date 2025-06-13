package com.example.solidconnection.university.dto;

import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.domain.UnivApplyInfo;

import java.util.List;

public record UniversityDetailResponse(
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
        int studentCapacity,
        String tuitionFeeType,
        String semesterAvailableForDispatch,
        List<LanguageRequirementResponse> languageRequirements,
        String detailsForLanguage,
        String gpaRequirement,
        String gpaRequirementCriteria,
        String semesterRequirement,
        String detailsForApply,
        String detailsForMajor,
        String detailsForAccommodation,
        String detailsForEnglishCourse,
        String details,
        String accommodationUrl,
        String englishCourseUrl) {

    public static UniversityDetailResponse of(
            University university,
            UnivApplyInfo univApplyInfo) {
        return new UniversityDetailResponse(
                univApplyInfo.getId(),
                univApplyInfo.getTerm(),
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
                univApplyInfo.getTuitionFeeType().getKoreanName(),
                univApplyInfo.getSemesterAvailableForDispatch().getKoreanName(),
                univApplyInfo.getLanguageRequirements().stream()
                        .map(LanguageRequirementResponse::from)
                        .toList(),
                univApplyInfo.getDetailsForLanguage(),
                univApplyInfo.getGpaRequirement(),
                univApplyInfo.getGpaRequirementCriteria(),
                univApplyInfo.getSemesterRequirement(),
                univApplyInfo.getDetailsForApply(),
                univApplyInfo.getDetailsForMajor(),
                univApplyInfo.getDetailsForAccommodation(),
                univApplyInfo.getDetailsForEnglishCourse(),
                univApplyInfo.getDetails(),
                university.getAccommodationUrl(),
                university.getEnglishCourseUrl()
        );
    }
}
