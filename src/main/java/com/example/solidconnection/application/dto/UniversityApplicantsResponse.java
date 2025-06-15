package com.example.solidconnection.application.dto;

import com.example.solidconnection.university.domain.UnivApplyInfo;

import java.util.List;

public record UniversityApplicantsResponse(
        String koreanName,
        int studentCapacity,
        String region,
        String country,
        List<ApplicantResponse> applicants) {

    public static UniversityApplicantsResponse of(UnivApplyInfo univApplyInfo, List<ApplicantResponse> applicant) {
        return new UniversityApplicantsResponse(
                univApplyInfo.getKoreanName(),
                univApplyInfo.getStudentCapacity(),
                univApplyInfo.getUniversity().getRegion().getKoreanName(),
                univApplyInfo.getUniversity().getCountry().getKoreanName(),
                applicant);
    }
}
