package com.example.solidconnection.application.service;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.VerifyStatus;
import com.example.solidconnection.application.dto.ApplicantResponse;
import com.example.solidconnection.application.dto.ApplicationsResponse;
import com.example.solidconnection.application.dto.UniversityApplicantsResponse;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import com.example.solidconnection.university.repository.custom.UniversityFilterRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.solidconnection.common.exception.ErrorCode.APPLICATION_NOT_APPROVED;

@RequiredArgsConstructor
@Service
public class ApplicationQueryService {

    private final ApplicationRepository applicationRepository;
    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;
    private final UniversityFilterRepositoryImpl universityFilterRepository;

    @Value("${university.term}")
    public String term;

    @Transactional(readOnly = true)
    public ApplicationsResponse getApplicants(SiteUser siteUser, String regionCode, String keyword) {
        // 1. 대학 ID 필터링 (regionCode, keyword)
        List<Long> universityIds = universityFilterRepository.findByRegionCodeAndKeywords(regionCode, List.of(keyword));
        if (universityIds.isEmpty()) return new ApplicationsResponse(List.of(), List.of(), List.of());

        // 2. 조건에 맞는 모든 Application 한 번에 조회
        List<Application> applications = applicationRepository.findApplicationsForChoices(universityIds, VerifyStatus.APPROVED, term);

        // 3. 대학정보 조회
        List<UniversityInfoForApply> universityInfos = universityInfoForApplyRepository.findByIdsWithUniversityAndLocation(universityIds);

        // 4. 지원서 분류 및 DTO 변환
        return classifyApplicationsByChoice(universityInfos, applications, siteUser);
    }

    @Transactional(readOnly = true)
    public ApplicationsResponse getApplicantsByUserApplications(SiteUser siteUser) {
        Application userLatestApplication = applicationRepository.getApplicationBySiteUserAndTerm(siteUser, term);

        List<Long> universityIds = Stream.of(
                        userLatestApplication.getFirstChoiceUniversityApplyInfoId(),
                        userLatestApplication.getSecondChoiceUniversityApplyInfoId(),
                        userLatestApplication.getThirdChoiceUniversityApplyInfoId()
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (universityIds.isEmpty()) {
            return new ApplicationsResponse(List.of(), List.of(), List.of());
        }

        List<Application> applications = applicationRepository.findApplicationsForChoices(universityIds, VerifyStatus.APPROVED, term);
        List<UniversityInfoForApply> universityInfos = universityInfoForApplyRepository.findByIdsWithUniversityAndLocation(universityIds);

        return classifyApplicationsByChoice(universityInfos, applications, siteUser);
    }

    private ApplicationsResponse classifyApplicationsByChoice(
            List<UniversityInfoForApply> universityInfos,
            List<Application> applications,
            SiteUser siteUser) {

        Map<Long, List<Application>> firstChoiceMap = new HashMap<>();
        Map<Long, List<Application>> secondChoiceMap = new HashMap<>();
        Map<Long, List<Application>> thirdChoiceMap = new HashMap<>();

        for (Application a : applications) {
            if (a.getFirstChoiceUniversityApplyInfoId() != null) {
                firstChoiceMap.computeIfAbsent(a.getFirstChoiceUniversityApplyInfoId(), k -> new ArrayList<>()).add(a);
            }
            if (a.getSecondChoiceUniversityApplyInfoId() != null) {
                secondChoiceMap.computeIfAbsent(a.getSecondChoiceUniversityApplyInfoId(), k -> new ArrayList<>()).add(a);
            }
            if (a.getThirdChoiceUniversityApplyInfoId() != null) {
                thirdChoiceMap.computeIfAbsent(a.getThirdChoiceUniversityApplyInfoId(), k -> new ArrayList<>()).add(a);
            }
        }

        List<UniversityApplicantsResponse> firstChoiceApplicants = universityInfos.stream()
                .map(uia -> UniversityApplicantsResponse.of(
                        uia,
                        firstChoiceMap.getOrDefault(uia.getId(), List.of()).stream()
                                .map(ap -> ApplicantResponse.of(ap, Objects.equals(siteUser.getId(), ap.getSiteUser().getId())))
                                .toList()))
                .toList();

        List<UniversityApplicantsResponse> secondChoiceApplicants = universityInfos.stream()
                .map(uia -> UniversityApplicantsResponse.of(
                        uia,
                        secondChoiceMap.getOrDefault(uia.getId(), List.of()).stream()
                                .map(ap -> ApplicantResponse.of(ap, Objects.equals(siteUser.getId(), ap.getSiteUser().getId())))
                                .toList()))
                .toList();

        List<UniversityApplicantsResponse> thirdChoiceApplicants = universityInfos.stream()
                .map(uia -> UniversityApplicantsResponse.of(
                        uia,
                        thirdChoiceMap.getOrDefault(uia.getId(), List.of()).stream()
                                .map(ap -> ApplicantResponse.of(ap, Objects.equals(siteUser.getId(), ap.getSiteUser().getId())))
                                .toList()))
                .toList();

        return new ApplicationsResponse(firstChoiceApplicants, secondChoiceApplicants, thirdChoiceApplicants);
    }

    @Transactional(readOnly = true)
    public void validateSiteUserCanViewApplicants(SiteUser siteUser) {
        VerifyStatus verifyStatus = applicationRepository.getApplicationBySiteUserAndTerm(siteUser, term).getVerifyStatus();
        if (verifyStatus != VerifyStatus.APPROVED) {
            throw new CustomException(APPLICATION_NOT_APPROVED);
        }
    }
}
