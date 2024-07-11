package com.example.solidconnection.university.dto;

import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "대학 세부 사항 응답 데이터")
public record UniversityDetailResponse(

        @Schema(description = "대학 지원을 위한 정보 id", example = "1")
        long id,

        @Schema(description = "모집 시기", example = "2024-2")
        String term,

        @Schema(description = "국문 이름", example = "그라츠 대학")
        String koreanName,

        @Schema(description = "영문 이름", example = "University of Graz")
        String englishName,

        @Schema(description = "서브스에서 사용되는 이름", example = "university_of_graz")
        String formatName,

        @Schema(description = "지역", example = "유럽")
        String region,

        @Schema(description = "국가", example = "오스트리아")
        String country,

        @Schema(description = "대학 홈페이지 URL", example = "http://www.graz.ac.kr")
        String homepageUrl,

        @Schema(description = "대학 로고 이미지 URL", example = "http://example.com/logo.jpg")
        String logoImageUrl,

        @Schema(description = "대학 배경 이미지 URL", example = "http://example.com/background.jpg")
        String backgroundImageUrl,

        @Schema(description = "현지에 대한 세부 사항", example = "Detailed information about local conditions.")
        String detailsForLocal,

        @Schema(description = "모집 인원", example = "2")
        int studentCapacity,

        @Schema(description = "등록금 유형", example = "본교납부형")
        String tuitionFeeType,

        @Schema(description = "파견 가능 학기", example = "1")
        String semesterAvailableForDispatch,

        @ArraySchema(arraySchema = @Schema(description = "어학 성적 요구사항"))
        List<LanguageRequirementResponse> languageRequirements,

        @Schema(description = "어학 성적 세부 사항", example = "Minimum TOEFL score required is 80.")
        String detailsForLanguage,

        @Schema(description = "GPA", example = "3.5")
        String gpaRequirement,

        @Schema(description = "GPA 계산 기준", example = "4.0")
        String gpaRequirementCriteria,

        @Schema(description = "필요 학기", example = "2")
        String semesterRequirement,

        @Schema(description = "지원에 대한 세부 사항", example = "Application process detailed here.")
        String detailsForApply,

        @Schema(description = "전공에 대한 세부 사항", example = "Major requirements detailed here.")
        String detailsForMajor,

        @Schema(description = "숙박에 대한 세부 사항", example = "Accommodation details provided.")
        String detailsForAccommodation,

        @Schema(description = "영어 과정 세부 사항", example = "English courses available for international students.")
        String detailsForEnglishCourse,

        @Schema(description = "기타 세부 사항", example = "Additional university details.")
        String details,

        @Schema(description = "숙박 시설 URL", example = "http://example.com/accommodation")
        String accommodationUrl,

        @Schema(description = "영어 수업 정보 URL", example = "http://example.com/englishCourses")
        String englishCourseUrl) {

    public static UniversityDetailResponse of(
            University university,
            UniversityInfoForApply universityInfoForApply) {
        return new UniversityDetailResponse(
                university.getId(),
                universityInfoForApply.getTerm(),
                university.getKoreanName(),
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
