package com.example.solidconnection.application.dto;

import com.example.solidconnection.university.domain.UniversityInfoForApply;

import java.util.List;

public record UniversityApplicantsResponse(
        String koreanName,
        int studentCapacity,
        String region,
        String country,
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
