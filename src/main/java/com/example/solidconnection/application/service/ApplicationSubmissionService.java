package com.example.solidconnection.application.service;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.VerifyStatus;
import com.example.solidconnection.application.dto.ApplicationSubmissionResponse;
import com.example.solidconnection.application.dto.ApplyRequest;
import com.example.solidconnection.application.dto.UniversityChoiceRequest;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.cache.annotation.DefaultCacheOut;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.example.solidconnection.common.exception.ErrorCode.APPLY_UPDATE_LIMIT_EXCEED;
import static com.example.solidconnection.common.exception.ErrorCode.GPA_SCORE_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_GPA_SCORE_STATUS;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_LANGUAGE_TEST_SCORE;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_LANGUAGE_TEST_SCORE_STATUS;

@RequiredArgsConstructor
@Service
public class ApplicationSubmissionService {

    public static final int APPLICATION_UPDATE_COUNT_LIMIT = 3;

    private final ApplicationRepository applicationRepository;
    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;
    private final GpaScoreRepository gpaScoreRepository;
    private final LanguageTestScoreRepository languageTestScoreRepository;

    @Value("${university.term}")
    private String term;

    // 학점 및 어학성적이 모두 유효한 경우에만 지원서 등록이 가능하다.
    // 기존에 있던 status field 우선 APRROVED로 입력시킨다.
    @Transactional
    // todo: 임시로 새로운 신청 생성 시 기존 캐싱 데이터를 삭제한다. 추후 수정 필요
    @DefaultCacheOut(
            key = {"applications:all"},
            cacheManager = "customCacheManager"
    )
    public ApplicationSubmissionResponse apply(SiteUser siteUser, ApplyRequest applyRequest) {
        UniversityChoiceRequest universityChoiceRequest = applyRequest.universityChoiceRequest();
        GpaScore gpaScore = getValidGpaScore(siteUser, applyRequest.gpaScoreId());
        LanguageTestScore languageTestScore = getValidLanguageTestScore(siteUser, applyRequest.languageTestScoreId());

        UniversityInfoForApply firstChoiceUniversity = universityInfoForApplyRepository
                .getUniversityInfoForApplyByIdAndTerm(universityChoiceRequest.firstChoiceUniversityId(), term);
        UniversityInfoForApply secondChoiceUniversity = Optional.ofNullable(universityChoiceRequest.secondChoiceUniversityId())
                .map(id -> universityInfoForApplyRepository.getUniversityInfoForApplyByIdAndTerm(id, term))
                .orElse(null);
        UniversityInfoForApply thirdChoiceUniversity = Optional.ofNullable(universityChoiceRequest.thirdChoiceUniversityId())
                .map(id -> universityInfoForApplyRepository.getUniversityInfoForApplyByIdAndTerm(id, term))
                .orElse(null);

        Optional<Application> existingApplication = applicationRepository.findBySiteUserAndTerm(siteUser, term);
        int updateCount = existingApplication
                .map(application -> {
                    validateUpdateLimitNotExceed(application);
                    application.setIsDeleteTrue();
                    return application.getUpdateCount() + 1;
                })
                .orElse(1);
        Application newApplication = new Application(siteUser, gpaScore.getGpa(), languageTestScore.getLanguageTest(),
                term, updateCount, firstChoiceUniversity, secondChoiceUniversity, thirdChoiceUniversity, getRandomNickname());
        newApplication.setVerifyStatus(VerifyStatus.APPROVED);
        applicationRepository.save(newApplication);
        return ApplicationSubmissionResponse.from(newApplication);
    }

    private GpaScore getValidGpaScore(SiteUser siteUser, Long gpaScoreId) {
        GpaScore gpaScore = gpaScoreRepository.findGpaScoreBySiteUserAndId(siteUser, gpaScoreId)
                .orElseThrow(() -> new CustomException(GPA_SCORE_NOT_FOUND));
        if (gpaScore.getVerifyStatus() != VerifyStatus.APPROVED) {
            throw new CustomException(INVALID_GPA_SCORE_STATUS);
        }
        return gpaScore;
    }

    private LanguageTestScore getValidLanguageTestScore(SiteUser siteUser, Long languageTestScoreId) {
        LanguageTestScore languageTestScore = languageTestScoreRepository
                .findLanguageTestScoreBySiteUserAndId(siteUser, languageTestScoreId)
                .orElseThrow(() -> new CustomException(INVALID_LANGUAGE_TEST_SCORE));
        if (languageTestScore.getVerifyStatus() != VerifyStatus.APPROVED) {
            throw new CustomException(INVALID_LANGUAGE_TEST_SCORE_STATUS);
        }
        return languageTestScore;
    }

    private String getRandomNickname() {
        String randomNickname = NicknameCreator.createRandomNickname();
        while (applicationRepository.existsByNicknameForApply(randomNickname)) {
            randomNickname = NicknameCreator.createRandomNickname();
        }
        return randomNickname;
    }

    private void validateUpdateLimitNotExceed(Application application) {
        if (application.getUpdateCount() >= APPLICATION_UPDATE_COUNT_LIMIT) {
            throw new CustomException(APPLY_UPDATE_LIMIT_EXCEED);
        }
    }
}
