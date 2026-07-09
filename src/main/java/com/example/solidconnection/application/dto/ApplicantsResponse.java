package com.example.solidconnection.application.dto;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import java.util.List;
import java.util.Objects;

public record ApplicantsResponse(
        String koreanName,
        Integer studentCapacity,
        String region,
        String country,
        String logoImageUrl,
        String backgroundImageUrl,
        List<ApplicantResponse> applicants) {

    public static ApplicantsResponse of(UnivApplyInfo univApplyInfo, List<Application> applications, SiteUser siteUser) {
        return new ApplicantsResponse(
                univApplyInfo.getKoreanName(),
                univApplyInfo.getStudentCapacity(),
                univApplyInfo.getUniversity().getRegion().getKoreanName(),
                univApplyInfo.getUniversity().getCountry().getKoreanName(),
                univApplyInfo.getUniversity().getLogoImageUrl(),
                univApplyInfo.getUniversity().getBackgroundImageUrl(),
                applications.stream()
                        .map(application -> ApplicantResponse.of(application, isUsers(application, siteUser)))
                        .toList());
    }

    private static boolean isUsers(Application application, SiteUser siteUser) {
        return Objects.equals(application.getSiteUserId(), siteUser.getId());
    }
}
