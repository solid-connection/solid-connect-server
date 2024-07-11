package com.example.solidconnection.application.dto;

import com.example.solidconnection.university.domain.UniversityInfoForApply;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "대학과 그 대학에 지원한 지원자 정보")
public record UniversityApplicantsResponse(
        @Schema(description = "대학의 한국어 이름", example = "괌대학")
        String koreanName,

        @Schema(description = "선발 인원", example = "4")
        int studentCapacity,

        @Schema(description = "지역", example = "영미권")
        String region,

        @Schema(description = "국가", example = "미국")
        String country,

        @ArraySchema(schema = @Schema(description = "지원자 목록", implementation = ApplicantResponse.class))
        List<ApplicantResponse> applicants) {

    public static UniversityApplicantsResponse of(UniversityInfoForApply universityInfoForApply, List<ApplicantResponse> applicant) {
        return new UniversityApplicantsResponse(
                universityInfoForApply.getUniversity().getKoreanName(),
                universityInfoForApply.getStudentCapacity(),
                universityInfoForApply.getUniversity().getRegion().getKoreanName(),
                universityInfoForApply.getUniversity().getCountry().getKoreanName(),
                applicant);
    }
}
