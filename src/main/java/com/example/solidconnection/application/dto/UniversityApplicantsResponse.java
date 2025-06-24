package com.example.solidconnection.application.dto;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.UniversityInfoForApply;

import java.util.List;

public record UniversityApplicantsResponse(
        String koreanName,
        int studentCapacity,
        String region,
        String country,
        List<ApplicantResponse> applicants) {
    public static UniversityApplicantsResponse of(UniversityInfoForApply universityInfoForApply, List<Application> applications, SiteUser siteUser) {
        return new UniversityApplicantsResponse(
                universityInfoForApply.getKoreanName(),
                universityInfoForApply.getStudentCapacity(),
                universityInfoForApply.getUniversity().getRegion().getKoreanName(),
                universityInfoForApply.getUniversity().getCountry().getKoreanName(),
                applications.stream()
                            .map(application -> ApplicantResponse.of(application, isUsers(application, siteUser)))
                            .toList());
    }

    private static boolean isUsers(Application application, SiteUser siteUser) {
        return application.getSiteUser().getId().equals(siteUser.getId());
    }
}
