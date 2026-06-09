package com.example.solidconnection.application.service;

import static com.example.solidconnection.common.exception.ErrorCode.APPLICATION_NOT_APPROVED;
import static com.example.solidconnection.common.exception.ErrorCode.CURRENT_TERM_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.ApplicationChoice;
import com.example.solidconnection.application.dto.ApplicantsResponse;
import com.example.solidconnection.application.dto.ApplicationsResponse;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.term.domain.Term;
import com.example.solidconnection.term.repository.TermRepository;
import com.example.solidconnection.university.domain.HomeUniversity;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.repository.HomeUniversityRepository;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import com.example.solidconnection.university.repository.custom.UnivApplyInfoFilterRepositoryImpl;
import io.micrometer.common.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ApplicationQueryService {

    private static final int DEFAULT_MAX_CHOICE_COUNT = 3;

    private final ApplicationRepository applicationRepository;
    private final UnivApplyInfoRepository univApplyInfoRepository;
    private final UnivApplyInfoFilterRepositoryImpl universityFilterRepository;
    private final SiteUserRepository siteUserRepository;
    private final TermRepository termRepository;
    private final HomeUniversityRepository homeUniversityRepository;

    @Transactional(readOnly = true)
    public ApplicationsResponse getApplicants(long siteUserId, String regionCode, String keyword) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        List<String> keywords = StringUtils.isNotBlank(keyword) ? List.of(keyword) : List.of();

        Term term = termRepository.findByIsCurrentTrue()
                .orElseThrow(() -> new CustomException(CURRENT_TERM_NOT_FOUND));

        List<UnivApplyInfo> univApplyInfos = universityFilterRepository
                .findAllByRegionCodeAndKeywordsAndTermId(regionCode, keywords, term.getId());
        if (univApplyInfos.isEmpty()) {
            return new ApplicationsResponse(List.of());
        }

        List<Long> univApplyInfoIds = univApplyInfos.stream()
                .map(UnivApplyInfo::getId)
                .toList();
        List<Application> applications = applicationRepository
                .findAllByUnivApplyInfoIds(univApplyInfoIds, VerifyStatus.APPROVED, term.getId());

        int maxChoiceCount = resolveMaxChoiceCount(siteUser);
        return classifyApplicationsByChoice(univApplyInfos, applications, siteUser, maxChoiceCount);
    }

    @Transactional(readOnly = true)
    public ApplicationsResponse getApplicantsByUserApplications(long siteUserId) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Term term = termRepository.findByIsCurrentTrue()
                .orElseThrow(() -> new CustomException(CURRENT_TERM_NOT_FOUND));

        Application userLatestApplication = applicationRepository
                .getApplicationBySiteUserIdAndTermId(siteUser.getId(), term.getId());

        List<Long> univApplyInfoIds = userLatestApplication.getChoices().stream()
                .map(ApplicationChoice::getUnivApplyInfoId)
                .distinct()
                .collect(Collectors.toList());

        if (univApplyInfoIds.isEmpty()) {
            return new ApplicationsResponse(List.of());
        }

        List<Application> applications = applicationRepository
                .findAllByUnivApplyInfoIds(univApplyInfoIds, VerifyStatus.APPROVED, term.getId());
        List<UnivApplyInfo> univApplyInfos = univApplyInfoRepository.findAllByIds(univApplyInfoIds);

        int maxChoiceCount = resolveMaxChoiceCount(siteUser);
        return classifyApplicationsByChoice(univApplyInfos, applications, siteUser, maxChoiceCount);
    }

    private ApplicationsResponse classifyApplicationsByChoice(
            List<UnivApplyInfo> univApplyInfos,
            List<Application> applications,
            SiteUser siteUser,
            int maxChoiceCount) {
        List<List<ApplicantsResponse>> allChoices = new ArrayList<>();
        for (int order = 1; order <= maxChoiceCount; order++) {
            final int choiceOrder = order;
            Map<Long, List<Application>> choiceMap = buildChoiceMapForOrder(applications, choiceOrder);
            allChoices.add(createUniversityApplicantsResponses(univApplyInfos, choiceMap, siteUser));
        }
        return new ApplicationsResponse(allChoices);
    }

    private int resolveMaxChoiceCount(SiteUser siteUser) {
        if (siteUser.getHomeUniversityId() == null) {
            return DEFAULT_MAX_CHOICE_COUNT;
        }
        return homeUniversityRepository.findById(siteUser.getHomeUniversityId())
                .map(HomeUniversity::getMaxChoiceCount)
                .orElse(DEFAULT_MAX_CHOICE_COUNT);
    }

    private Map<Long, List<Application>> buildChoiceMapForOrder(List<Application> applications, int order) {
        Map<Long, List<Application>> map = new HashMap<>();
        for (Application application : applications) {
            application.getChoices().stream()
                    .filter(c -> c.getChoiceOrder() == order)
                    .findFirst()
                    .ifPresent(choice -> map
                            .computeIfAbsent(choice.getUnivApplyInfoId(), k -> new ArrayList<>())
                            .add(application));
        }
        return map;
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
    public void validateSiteUserCanViewApplicants(long siteUserId) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Term term = termRepository.findByIsCurrentTrue()
                .orElseThrow(() -> new CustomException(CURRENT_TERM_NOT_FOUND));

        VerifyStatus verifyStatus = applicationRepository
                .getApplicationBySiteUserIdAndTermId(siteUser.getId(), term.getId()).getVerifyStatus();
        if (verifyStatus != VerifyStatus.APPROVED) {
            throw new CustomException(APPLICATION_NOT_APPROVED);
        }
    }
}
