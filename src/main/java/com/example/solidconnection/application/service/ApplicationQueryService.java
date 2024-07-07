package com.example.solidconnection.application.service;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.dto.ApplicantResponse;
import com.example.solidconnection.application.dto.ApplicationsResponse;
import com.example.solidconnection.application.dto.UniversityApplicantsResponse;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.SiteUser;
import com.example.solidconnection.entity.University;
import com.example.solidconnection.entity.UniversityInfoForApply;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.CountryCode;
import com.example.solidconnection.type.RegionCode;
import com.example.solidconnection.type.VerifyStatus;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import com.example.solidconnection.university.repository.custom.UniversityRepositoryForFilterImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.example.solidconnection.constants.Constants.TERM;
import static com.example.solidconnection.custom.exception.ErrorCode.APPLICATION_NOT_APPROVED;

@RequiredArgsConstructor
@Transactional
@Service
public class ApplicationQueryService {

    private final ApplicationRepository applicationRepository;
    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;
    private final SiteUserRepository siteUserRepository;
    private final UniversityRepositoryForFilterImpl universityRepositoryForFilter;

    /*
     * 다른 지원자들의 성적을 조회한다.
     * - 유저가 다른 지원자들을 볼 수 있는지 검증한다.
     * - 지역과 키워드를 통해 대학을 필터링한다.
     * - 대학에 따른 '지원을 위한 대학 정보(university for apply)'를 조회하고, 그 대학에 지원한 다른 지원자들의 정보를 조회한다.
     * */
    public ApplicationsResponse getApplicants(String email, String region, String keyword) {
        // 유저가 다른 지원자들을 볼 수 있는지 검증
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        validateSiteUserCanViewApplicants(siteUser);

        // 한국어 키워드를 통해 국가를 조회
        RegionCode regionCode = RegionCode.getRegionCodeByKoreanName(region);
        List<CountryCode> countryCodes = null;
        if (!keyword.isBlank()) {
            countryCodes = CountryCode.getCountryCodeMatchesToKeyword(List.of(keyword));
        }

        // 국가와 키워드와 지역을 통해 대학 정보를 판별
        List<University> universities = universityRepositoryForFilter.findByRegionAndCountryAndKeyword(regionCode, countryCodes, List.of(keyword));

        // 1지망, 2지망 지원자들을 조회
        List<UniversityApplicantsResponse> firstChoiceApplicants = getApplicantsByChoice(
                universities,
                siteUser,
                uia -> applicationRepository.findAllByFirstChoiceUniversityAndVerifyStatus(uia, VerifyStatus.APPROVED)
        );
        List<UniversityApplicantsResponse> secondChoiceApplicants = getApplicantsByChoice(
                universities,
                siteUser,
                uia -> applicationRepository.findAllBySecondChoiceUniversityAndVerifyStatus(uia, VerifyStatus.APPROVED)
        );

        return new ApplicationsResponse(firstChoiceApplicants, secondChoiceApplicants);
    }

    private void validateSiteUserCanViewApplicants(SiteUser siteUser) {
        VerifyStatus verifyStatus = applicationRepository.getBySiteUser(siteUser).getVerifyStatus();
        if (verifyStatus != VerifyStatus.APPROVED) {
            throw new CustomException(APPLICATION_NOT_APPROVED);
        }
    }

    private List<UniversityApplicantsResponse> getApplicantsByChoice(List<University> searchedUniversities, SiteUser siteUser, Function<UniversityInfoForApply, List<Application>> findApplicationsByChoice) {
        return universityInfoForApplyRepository.findByUniversitiesAndTerm(searchedUniversities, TERM).stream()
                .map(universityInfoForApply ->
                        UniversityApplicantsResponse.of(
                                universityInfoForApply,
                                findApplicationsByChoice.apply(universityInfoForApply).stream()
                                        .map(ap -> ApplicantResponse.of(ap, Objects.equals(siteUser.getId(), ap.getSiteUser().getId())))
                                        .toList()
                        ))
                .toList();
    }
}
