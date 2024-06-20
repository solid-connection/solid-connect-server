package com.example.solidconnection.application.service;

import com.example.solidconnection.application.dto.*;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.constants.NicknameForApplyWords;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.Application;
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
import java.util.Random;

import static com.example.solidconnection.constants.Constants.APPLICATION_UPDATE_COUNT_LIMIT;
import static com.example.solidconnection.constants.Constants.TERM;
import static com.example.solidconnection.custom.exception.ErrorCode.*;

@RequiredArgsConstructor
@Transactional
@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;
    private final UniversityValidator universityValidator;
    private final SiteUserRepository siteUserRepository;
    private final UniversityRepositoryForFilterImpl universityRepositoryForFilter;

    public boolean submitScore(String email, ScoreRequestDto scoreRequestDto) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);

        // 수정
        if (applicationRepository.existsBySiteUser_Email(email)) {
            Application application = applicationRepository.getBySiteUser_Email(email);
            application.setGpa(scoreRequestDto.getGpa());
            application.setGpaCriteria(scoreRequestDto.getGpaCriteria());
            application.setGpaReportUrl(scoreRequestDto.getGpaReportUrl());
            application.setLanguageTestScore(scoreRequestDto.getLanguageTestScore());
            application.setLanguageTestType(scoreRequestDto.getLanguageTestType());
            application.setLanguageTestReportUrl(scoreRequestDto.getLanguageTestReportUrl());
            application.setVerifyStatus(VerifyStatus.PENDING);
            return true;
        }

        // 최초 등록
        Application application = Application.saveScore(siteUser, scoreRequestDto);
        applicationRepository.save(application);
        return true;
    }

    public boolean submitUniversityChoice(String email, UniversityRequestDto universityRequestDto) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);

        // 저장에 필요한 엔티티 불러오기 or 생성
        UniversityInfoForApply firstChoiceUniversity = universityValidator.getValidatedUniversityInfoForApplyByIdAndTerm(universityRequestDto.getFirstChoiceUniversityId());
        UniversityInfoForApply secondChoiceUniversity;
        try {
            secondChoiceUniversity = universityValidator.getValidatedUniversityInfoForApplyByIdAndTerm(universityRequestDto.getSecondChoiceUniversityId());
        } catch (Exception e) {
            secondChoiceUniversity = null;
        }

        // 1,2 동일한 대학교 지망 에러 처리
        if (secondChoiceUniversity != null && Objects.equals(secondChoiceUniversity.getId(), firstChoiceUniversity.getId())) {
            throw new CustomException(CANT_APPLY_FOR_SAME_UNIVERSITY);
        }

        Application application;

        // 대학 최초 등록이면
        if (applicationRepository.findBySiteUser_Email(siteUser.getEmail()).isEmpty()) {
            application = Application.saveUniversity(siteUser, firstChoiceUniversity, secondChoiceUniversity);
            application.setNicknameForApply(makeRandomNickname());
            applicationRepository.save(application);
            return true;
        }

        // 수정 횟수 초과 에러 처리
        application = applicationRepository.getBySiteUser_Email(email);
        if (application.getUpdateCount() > APPLICATION_UPDATE_COUNT_LIMIT) {
            throw new CustomException(APPLY_UPDATE_LIMIT_EXCEED);
        }

        // 수정이면 update count 1 증가
        if (application.getFirstChoiceUniversity() != null) {
            application.setUpdateCount(application.getUpdateCount() + 1);
        }

        // 수정
        application.setFirstChoiceUniversity(firstChoiceUniversity);
        application.setSecondChoiceUniversity(secondChoiceUniversity);

        // 새로운 닉네임 부여
        String randomNickname = makeRandomNickname();
        while (applicationRepository.existsByNicknameForApply(randomNickname)) {
            randomNickname = makeRandomNickname();
        }
        application.setNicknameForApply(randomNickname);

        return true;
    }

    private String makeRandomNickname() {
        Random random = new Random();
        int randomIndex1 = random.nextInt(NicknameForApplyWords.adjectives.size());
        String randomAdjective = NicknameForApplyWords.adjectives.get(randomIndex1);
        int randomIndex2 = random.nextInt(NicknameForApplyWords.nouns.size());
        String randomNoun = NicknameForApplyWords.nouns.get(randomIndex2);
        return randomAdjective + " " + randomNoun;
    }

    public ApplicationsDto getApplicants(String email, String region, String keyword) {
        // 유저 검증
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        // 지원했는지 검증
        Application application = applicationRepository.getBySiteUser_Email(email);
        // 승인되었는지 확인
        validateApproved(application);

        RegionCode regionCode = null;
        if (region != null && !region.isBlank()) {
            regionCode = RegionCode.getRegionCodeByKoreanName(region);
        }
        List<CountryCode> countryCodes = null;
        if (keyword != null && !keyword.isBlank()) {
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
                            .region(university.getRegion().getCode().getKoreanName())
                            .country(university.getCountry().getCode().getKoreanName())
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
                            .region(university.getRegion().getCode().getKoreanName())
                            .country(university.getCountry().getCode().getKoreanName())
                            .applicants(secondChoiceApplicant)
                            .build();
                })
                .toList();
    }

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
            if (application.get().getGpaReportUrl() == null) {
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
