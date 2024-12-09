package com.example.solidconnection.university.dto;

import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.domain.UniversityInfoForApply;

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
            UniversityInfoForApply universityInfoForApply) {
        return new UniversityDetailResponse(
                universityInfoForApply.getId(),
                universityInfoForApply.getTerm(),
                universityInfoForApply.getKoreanName(),
                university.getEnglishName(),
                university.getFormatName(),
                university.getRegion().getKoreanName(),
                university.getCountry().getKoreanName(),
                university.getHomepageUrl(),
                university.getLogoImageUrl(),
                university.getBackgroundImageUrl(),
                university.getDetailsForLocal(),
                universityInfoForApply.getStudentCapacity(),
                universityInfoForApply.getTuitionFeeType().getKoreanName(),
                universityInfoForApply.getSemesterAvailableForDispatch().getKoreanName(),
                universityInfoForApply.getLanguageRequirements().stream()
                        .map(LanguageRequirementResponse::from)
                        .toList(),
                universityInfoForApply.getDetailsForLanguage(),
                universityInfoForApply.getGpaRequirement(),
                universityInfoForApply.getGpaRequirementCriteria(),
                universityInfoForApply.getSemesterRequirement(),
                universityInfoForApply.getDetailsForApply(),
                universityInfoForApply.getDetailsForMajor(),
                universityInfoForApply.getDetailsForAccommodation(),
                universityInfoForApply.getDetailsForEnglishCourse(),
                universityInfoForApply.getDetails(),
                university.getAccommodationUrl(),
                university.getEnglishCourseUrl()
        );
    }
}
