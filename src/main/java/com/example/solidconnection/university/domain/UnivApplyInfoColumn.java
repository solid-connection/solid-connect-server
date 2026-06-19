package com.example.solidconnection.university.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UnivApplyInfoColumn {

    UNIVERSITY_KOREAN_NAME("universityKoreanName"),
    UNIVERSITY_ENGLISH_NAME("universityEnglishName"),
    UNIVERSITY_FORMAT_NAME("universityFormatName"),
    UNIVERSITY_COUNTRY_CODE("universityCountryCode"),
    UNIVERSITY_HOMEPAGE_URL("universityHomepageUrl"),
    UNIVERSITY_ENGLISH_COURSE_URL("universityEnglishCourseUrl"),
    UNIVERSITY_ACCOMMODATION_URL("universityAccommodationUrl"),
    UNIVERSITY_DETAILS_FOR_LOCAL("universityDetailsForLocal"),
    STUDENT_CAPACITY("studentCapacity"),
    SEMESTER_AVAILABLE_FOR_DISPATCH("semesterAvailableForDispatch"),
    SEMESTER_REQUIREMENT("semesterRequirement"),
    DETAILS_FOR_LANGUAGE("detailsForLanguage"),
    GPA_REQUIREMENT("gpaRequirement"),
    GPA_REQUIREMENT_CRITERIA("gpaRequirementCriteria"),
    DETAILS_FOR_ACCOMMODATION("detailsForAccommodation"),
    ;

    private final String fieldName;
}
