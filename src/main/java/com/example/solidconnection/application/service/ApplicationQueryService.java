package com.example.solidconnection.application.service;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.application.dto.ApplicationsResponse;
import com.example.solidconnection.application.dto.ApplicantsResponse;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import com.example.solidconnection.university.repository.custom.UnivApplyInfoFilterRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.solidconnection.common.exception.ErrorCode.APPLICATION_NOT_APPROVED;

@RequiredArgsConstructor
@Service
public class ApplicationQueryService {

    private final ApplicationRepository applicationRepository;
    private final UnivApplyInfoRepository univApplyInfoRepository;
    private final UnivApplyInfoFilterRepositoryImpl universityFilterRepository;

    @Value("${university.term}")
    public String term;

    // todo: 캐싱 정책 변경 시 수정 필요
    @Transactional(readOnly = true)
    public ApplicationsResponse getApplicants(SiteUser siteUser, String regionCode, String keyword) {
        // 1. 대학 지원 정보 필터링 (regionCode, keyword)
        List<UnivApplyInfo> univApplyInfos = universityFilterRepository.findAllByRegionCodeAndKeywords(regionCode, List.of(keyword));
        if (univApplyInfos.isEmpty()) {
            return new ApplicationsResponse(List.of(), List.of(), List.of());
        }
        // 2. 조건에 맞는 모든 Application 한 번에 조회
        List<Long> univApplyInfoIds = univApplyInfos.stream()
                .map(UnivApplyInfo::getId)
                .toList();
        List<Application> applications = applicationRepository.findAllByUnivApplyInfoIds(univApplyInfoIds, VerifyStatus.APPROVED, term);
        // 3. 지원서 분류 및 DTO 변환
        return classifyApplicationsByChoice(univApplyInfos, applications, siteUser);
    }

    @Transactional(readOnly = true)
    public ApplicationsResponse getApplicantsByUserApplications(SiteUser siteUser) {
        Application userLatestApplication = applicationRepository.getApplicationBySiteUserAndTerm(siteUser, term);

        List<Long> univApplyInfoIds = Stream.of(
                        userLatestApplication.getFirstChoiceUnivApplyInfoId(),
                        userLatestApplication.getSecondChoiceUnivApplyInfoId(),
                        userLatestApplication.getThirdChoiceUnivApplyInfoId()
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (univApplyInfoIds.isEmpty()) {
            return new ApplicationsResponse(List.of(), List.of(), List.of());
        }

        List<Application> applications = applicationRepository.findAllByUnivApplyInfoIds(univApplyInfoIds, VerifyStatus.APPROVED, term);
        List<UnivApplyInfo> univApplyInfos = univApplyInfoRepository.findAllByIds(univApplyInfoIds);

        return classifyApplicationsByChoice(univApplyInfos, applications, siteUser);
    }

    private ApplicationsResponse classifyApplicationsByChoice(
            List<UnivApplyInfo> univApplyInfos,
            List<Application> applications,
            SiteUser siteUser) {
        Map<Long, List<Application>> firstChoiceMap = createChoiceMap(applications, Application::getFirstChoiceUnivApplyInfoId);
        Map<Long, List<Application>> secondChoiceMap = createChoiceMap(applications, Application::getSecondChoiceUnivApplyInfoId);
        Map<Long, List<Application>> thirdChoiceMap = createChoiceMap(applications, Application::getThirdChoiceUnivApplyInfoId);

        List<ApplicantsResponse> firstChoiceApplicants =
                createUniversityApplicantsResponses(univApplyInfos, firstChoiceMap, siteUser);
        List<ApplicantsResponse> secondChoiceApplicants =
                createUniversityApplicantsResponses(univApplyInfos, secondChoiceMap, siteUser);
        List<ApplicantsResponse> thirdChoiceApplicants =
                createUniversityApplicantsResponses(univApplyInfos, thirdChoiceMap, siteUser);

        return new ApplicationsResponse(firstChoiceApplicants, secondChoiceApplicants, thirdChoiceApplicants);
    }

    private Map<Long, List<Application>> createChoiceMap(
            List<Application> applications,
            Function<Application, Long> choiceIdExtractor) {
        Map<Long, List<Application>> choiceMap = new HashMap<>();

        for (Application application : applications) {
            Long choiceId = choiceIdExtractor.apply(application);
            if (choiceId != null) {
                choiceMap.computeIfAbsent(choiceId, k -> new ArrayList<>()).add(application);
            }
        }

        return choiceMap;
    }

    private List<ApplicantsResponse> createUniversityApplicantsResponses(
            List<UnivApplyInfo> univApplyInfos,
            Map<Long, List<Application>> choiceMap,
            SiteUser siteUser) {
        return univApplyInfos.stream()
                .map(uia -> ApplicantsResponse.of(uia, choiceMap.getOrDefault(uia.getId(), List.of()), siteUser))
                .toList();
    }

    @Transactional(readOnly = true)
    public void validateSiteUserCanViewApplicants(SiteUser siteUser) {
        VerifyStatus verifyStatus = applicationRepository.getApplicationBySiteUserAndTerm(siteUser, term).getVerifyStatus();
        if (verifyStatus != VerifyStatus.APPROVED) {
            throw new CustomException(APPLICATION_NOT_APPROVED);
        }
    }
}
