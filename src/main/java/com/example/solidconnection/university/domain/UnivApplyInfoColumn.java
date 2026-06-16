package com.example.solidconnection.university.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UnivApplyInfoColumn {

    UNIVERSITY_KOREAN_NAME("universityKoreanName"),
    ENGLISH_NAME("englishName"),
    FORMAT_NAME("formatName"),
    COUNTRY_CODE("countryCode"),
    HOMEPAGE_URL("homepageUrl"),
    ENGLISH_COURSE_URL("englishCourseUrl"),
    ACCOMMODATION_URL("accommodationUrl"),
    DETAILS_FOR_LOCAL("detailsForLocal"),
    STUDENT_CAPACITY("studentCapacity"),
    TUITION_FEE_TYPE("tuitionFeeType"),
    SEMESTER_AVAILABLE_FOR_DISPATCH("semesterAvailableForDispatch"),
    SEMESTER_REQUIREMENT("semesterRequirement"),
    DETAILS_FOR_LANGUAGE("detailsForLanguage"),
    GPA_REQUIREMENT("gpaRequirement"),
    GPA_REQUIREMENT_CRITERIA("gpaRequirementCriteria"),
    DETAILS_FOR_APPLY("detailsForApply"),
    DETAILS_FOR_MAJOR("detailsForMajor"),
    DETAILS_FOR_ACCOMMODATION("detailsForAccommodation"),
    DETAILS_FOR_ENGLISH_COURSE("detailsForEnglishCourse"),
    DETAILS("details"),
    ;

    private final String fieldName;
}
