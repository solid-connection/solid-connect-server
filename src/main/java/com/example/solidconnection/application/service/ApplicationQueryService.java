package com.example.solidconnection.application.service;

import com.example.solidconnection.application.dto.ApplicantDto;
import com.example.solidconnection.application.dto.ApplicationsDto;
import com.example.solidconnection.application.dto.UniversityApplicantsDto;
import com.example.solidconnection.application.dto.VerifyStatusDto;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.entity.SiteUser;
import com.example.solidconnection.entity.University;
import com.example.solidconnection.entity.UniversityInfoForApply;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.ApplicationStatusResponse;
import com.example.solidconnection.type.CountryCode;
import com.example.solidconnection.type.RegionCode;
import com.example.solidconnection.type.VerifyStatus;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import com.example.solidconnection.university.repository.custom.UniversityRepositoryForFilterImpl;
import com.example.solidconnection.university.service.UniversityValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.example.solidconnection.constants.Constants.TERM;
import static com.example.solidconnection.custom.exception.ErrorCode.APPLICATION_NOT_APPROVED;

@RequiredArgsConstructor
@Transactional
@Service
public class ApplicationQueryService {

    private final ApplicationRepository applicationRepository;
    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;
    private final UniversityValidator universityValidator;
    private final SiteUserRepository siteUserRepository;
    private final UniversityRepositoryForFilterImpl universityRepositoryForFilter;

    /*
    * 지원자들의 성적을 조회한다.
    * - 지역과 키워드를 통해 필터링한다.
    * */
    public ApplicationsDto getApplicants(String email, String region, String keyword) {
        // 유저 검증
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        // 지원했는지 검증
        Application application = applicationRepository.getBySiteUser_Email(email);
        // 승인되었는지 확인
        validateApproved(application);

        RegionCode regionCode = RegionCode.getRegionCodeByKoreanName(region);
        List<CountryCode> countryCodes = null;
        if (!keyword.isBlank()) {
            countryCodes = CountryCode.getCountryCodeMatchesToKeyword(List.of(keyword));
        }

        List<University> universities = universityRepositoryForFilter.findByRegionAndCountryAndKeyword(regionCode, countryCodes, List.of(keyword));
        List<UniversityApplicantsDto> firstChoiceApplicants = getFirstChoiceApplicants(universities, siteUser);
        List<UniversityApplicantsDto> secondChoiceApplicants = getSecondChoiceApplicants(universities, siteUser);
        return ApplicationsDto.builder()
                .firstChoice(firstChoiceApplicants)
                .secondChoice(secondChoiceApplicants)
                .build();
    }

    private void validateApproved(Application application) {
        if (application.getVerifyStatus() != VerifyStatus.APPROVED) {
            throw new CustomException(APPLICATION_NOT_APPROVED);
        }
    }

    private List<UniversityApplicantsDto> getFirstChoiceApplicants(List<University> universities, SiteUser siteUser) {
        return universities.stream()
                .filter(university -> universityInfoForApplyRepository.existsByUniversityAndTerm(university, TERM))
                .map(university -> {
                    UniversityInfoForApply universityInfoForApply = universityValidator.getValidatedUniversityInfoForApplyByUniversityAndTerm(university);
                    List<Application> firstChoiceApplication = applicationRepository.findAllByFirstChoiceUniversityAndVerifyStatus(universityInfoForApply, VerifyStatus.APPROVED);
                    List<ApplicantDto> firstChoiceApplicant = firstChoiceApplication.stream()
                            .map(ap -> ApplicantDto.fromEntity(ap, Objects.equals(siteUser.getId(), ap.getSiteUser().getId())))
                            .toList();
                    return UniversityApplicantsDto.builder()
                            .koreanName(university.getKoreanName())
                            .studentCapacity(universityInfoForApply.getStudentCapacity())
                            .region(university.getRegion().getKoreanName())
                            .country(university.getCountry().getKoreanName())
                            .applicants(firstChoiceApplicant)
                            .build();
                })
                .toList();
    }

    private List<UniversityApplicantsDto> getSecondChoiceApplicants(List<University> universities, SiteUser siteUser) {
        return universities.stream()
                .filter(university -> universityInfoForApplyRepository.existsByUniversityAndTerm(university, TERM))
                .map(university -> {
                    UniversityInfoForApply universityInfoForApply = universityValidator.getValidatedUniversityInfoForApplyByUniversityAndTerm(university);
                    List<Application> secondChoiceApplication = applicationRepository.findAllBySecondChoiceUniversityAndVerifyStatus(universityInfoForApply, VerifyStatus.APPROVED);
                    List<ApplicantDto> secondChoiceApplicant = secondChoiceApplication.stream()
                            .map(ap -> ApplicantDto.fromEntity(ap, Objects.equals(siteUser.getId(), ap.getSiteUser().getId())))
                            .toList();
                    return UniversityApplicantsDto.builder()
                            .koreanName(university.getKoreanName())
                            .studentCapacity(universityInfoForApply.getStudentCapacity())
                            .region(university.getRegion().getKoreanName())
                            .country(university.getCountry().getKoreanName())
                            .applicants(secondChoiceApplicant)
                            .build();
                })
                .toList();
    }

    /*
     * 지원 상태를 조회한다.
     * */
    public VerifyStatusDto getVerifyStatus(String email) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        Optional<Application> application = applicationRepository.findBySiteUser_Email(siteUser.getEmail());

        // 아무것도 제출 안함
        if (application.isEmpty()) {
            return new VerifyStatusDto(ApplicationStatusResponse.NOT_SUBMITTED.name());
        }

        int updateCount = application.get().getUpdateCount();
        // 제출한 상태
        if (application.get().getVerifyStatus() == VerifyStatus.PENDING) {
            // 지망 대학만 제출
            if (application.get().getGpa().getGpaReportUrl() == null) {
                return new VerifyStatusDto(ApplicationStatusResponse.COLLEGE_SUBMITTED.name(), updateCount);
            }
            // 성적만 제출
            if (application.get().getFirstChoiceUniversity() == null) {
                return new VerifyStatusDto(ApplicationStatusResponse.SCORE_SUBMITTED.name(), 0);
            }
            // 성적 승인 대기 중
            return new VerifyStatusDto(ApplicationStatusResponse.SUBMITTED_PENDING.name(), updateCount);
        }
        // 성적 승인 반려
        if (application.get().getVerifyStatus() == VerifyStatus.REJECTED) {
            return new VerifyStatusDto(ApplicationStatusResponse.SUBMITTED_REJECTED.name(), updateCount);
        }
        // 성적 승인 완료
        return new VerifyStatusDto(ApplicationStatusResponse.SUBMITTED_APPROVED.name(), updateCount);
    }
}
