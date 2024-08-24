package com.example.solidconnection.application.service;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.dto.ApplicantResponse;
import com.example.solidconnection.application.dto.ApplicationsResponse;
import com.example.solidconnection.application.dto.UniversityApplicantsResponse;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.VerifyStatus;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import com.example.solidconnection.university.repository.custom.UniversityFilterRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.example.solidconnection.custom.exception.ErrorCode.APPLICATION_NOT_APPROVED;

@RequiredArgsConstructor
@Service
public class ApplicationQueryService {

    private final ApplicationRepository applicationRepository;
    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;
    private final SiteUserRepository siteUserRepository;
    private final UniversityFilterRepositoryImpl universityFilterRepository;
    @Value("${university.term}")
    public String term;

    /*
     * 다른 지원자들의 성적을 조회한다.
     * - 유저가 다른 지원자들을 볼 수 있는지 검증한다.
     * - 지역과 키워드를 통해 대학을 필터링한다.
     *   - 지역은 영어 대문자로 받는다 e.g. ASIA
     * - 1지망, 2지망 지원자들을 조회한다.
     * */
    @Transactional(readOnly = true)
    public ApplicationsResponse getApplicants(String email, String regionCode, String keyword) {
        // 유저가 다른 지원자들을 볼 수 있는지 검증
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        validateSiteUserCanViewApplicants(siteUser);

        // 국가와 키워드와 지역을 통해 대학을 필터링한다.
        List<University> universities
                = universityFilterRepository.findByRegionCodeAndKeywords(regionCode, List.of(keyword));

        // 1지망, 2지망, 3지망 지원자들을 조회한다.
        List<UniversityApplicantsResponse> firstChoiceApplicants = getFirstChoiceApplicants(universities, siteUser, term);
        List<UniversityApplicantsResponse> secondChoiceApplicants = getSecondChoiceApplicants(universities, siteUser, term);
        List<UniversityApplicantsResponse> thirdChoiceApplicants = getThirdChoiceApplicants(universities, siteUser, term);
        return new ApplicationsResponse(firstChoiceApplicants, secondChoiceApplicants, thirdChoiceApplicants);
    }

    private void validateSiteUserCanViewApplicants(SiteUser siteUser) {
        VerifyStatus verifyStatus = applicationRepository.getApplicationBySiteUser(siteUser).getVerifyStatus();
        if (verifyStatus != VerifyStatus.APPROVED) {
            throw new CustomException(APPLICATION_NOT_APPROVED);
        }
    }

    private List<UniversityApplicantsResponse> getFirstChoiceApplicants(List<University> universities, SiteUser siteUser, String term) {
        return getApplicantsByChoice(
                universities,
                siteUser,
                uia -> applicationRepository.findAllByFirstChoiceUniversityAndVerifyStatusAndTerm(uia, VerifyStatus.APPROVED, term)
        );
    }

    private List<UniversityApplicantsResponse> getSecondChoiceApplicants(List<University> universities, SiteUser siteUser, String term) {
        return getApplicantsByChoice(
                universities,
                siteUser,
                uia -> applicationRepository.findAllBySecondChoiceUniversityAndVerifyStatusAndTerm(uia, VerifyStatus.APPROVED, term)
        );
    }

    private List<UniversityApplicantsResponse> getThirdChoiceApplicants(List<University> universities, SiteUser siteUser, String term) {
        return getApplicantsByChoice(
                universities,
                siteUser,
                uia -> applicationRepository.findAllByThirdChoiceUniversityAndVerifyStatusAndTerm(uia, VerifyStatus.APPROVED, term)
        );
    }

    private List<UniversityApplicantsResponse> getApplicantsByChoice(
            List<University> searchedUniversities,
            SiteUser siteUser,
            Function<UniversityInfoForApply, List<Application>> findApplicationsByChoice) {
        return universityInfoForApplyRepository.findByUniversitiesAndTerm(searchedUniversities, term).stream()
                .map(universityInfoForApply -> UniversityApplicantsResponse.of(
                        universityInfoForApply,
                        findApplicationsByChoice.apply(universityInfoForApply).stream()
                                .map(ap -> ApplicantResponse.of(
                                        ap,
                                        Objects.equals(siteUser.getId(), ap.getSiteUser().getId())))
                                .toList()))
                .toList();
    }
}
