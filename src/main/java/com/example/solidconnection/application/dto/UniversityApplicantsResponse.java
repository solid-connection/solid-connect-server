package com.example.solidconnection.application.dto;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.UnivApplyInfo;

import java.util.List;

public record UniversityApplicantsResponse(
        String koreanName,
        int studentCapacity,
        String region,
        String country,
        List<ApplicantResponse> applicants) {
    public static UniversityApplicantsResponse of(UnivApplyInfo univApplyInfo, List<Application> applications, SiteUser siteUser) {
        return new UniversityApplicantsResponse(
                univApplyInfo.getKoreanName(),
                univApplyInfo.getStudentCapacity(),
                univApplyInfo.getUniversity().getRegion().getKoreanName(),
                univApplyInfo.getUniversity().getCountry().getKoreanName(),
                applications.stream()
                            .map(application -> ApplicantResponse.of(application, isUsers(application, siteUser)))
                            .toList());
    }

    private static boolean isUsers(Application application, SiteUser siteUser) {
        return application.getSiteUser().getId().equals(siteUser.getId());
    }
}
