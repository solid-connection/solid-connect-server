package com.example.solidconnection.application.service;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.dto.ApplyRequest;
import com.example.solidconnection.application.dto.UniversityChoiceRequest;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.VerifyStatus;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.example.solidconnection.custom.exception.ErrorCode.APPLY_UPDATE_LIMIT_EXCEED;
import static com.example.solidconnection.custom.exception.ErrorCode.CANT_APPLY_FOR_SAME_UNIVERSITY;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_GPA_SCORE;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_GPA_SCORE_STATUS;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_LANGUAGE_TEST_SCORE;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_LANGUAGE_TEST_SCORE_STATUS;

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
    public boolean apply(SiteUser siteUser, ApplyRequest applyRequest) {
        UniversityChoiceRequest universityChoiceRequest = applyRequest.universityChoiceRequest();

        Long gpaScoreId = applyRequest.gpaScoreId();
        Long languageTestScoreId = applyRequest.languageTestScoreId();
        GpaScore gpaScore = getValidGpaScore(siteUser, gpaScoreId);
        LanguageTestScore languageTestScore = getValidLanguageTestScore(siteUser, languageTestScoreId);

        Optional<Application> application = applicationRepository.findBySiteUserAndTerm(siteUser, term);

        UniversityInfoForApply firstChoiceUniversity = universityInfoForApplyRepository
                .getUniversityInfoForApplyByIdAndTerm(universityChoiceRequest.firstChoiceUniversityId(), term);
        UniversityInfoForApply secondChoiceUniversity = Optional.ofNullable(universityChoiceRequest.secondChoiceUniversityId())
                .map(id -> universityInfoForApplyRepository.getUniversityInfoForApplyByIdAndTerm(id, term))
                .orElse(null);
        UniversityInfoForApply thirdChoiceUniversity = Optional.ofNullable(universityChoiceRequest.thirdChoiceUniversityId())
                .map(id -> universityInfoForApplyRepository.getUniversityInfoForApplyByIdAndTerm(id, term))
                .orElse(null);

        if (application.isEmpty()) {
            Application newApplication = new Application(siteUser, gpaScore.getGpa(), languageTestScore.getLanguageTest(),
                    term, firstChoiceUniversity, secondChoiceUniversity, thirdChoiceUniversity, getRandomNickname());
            newApplication.setVerifyStatus(VerifyStatus.APPROVED);
            applicationRepository.save(newApplication);
        } else {
            Application before = application.get();
            validateUpdateLimitNotExceed(before);
            before.setIsDeleteTrue(); // 기존 이력 soft delete 수행한다.

            Application newApplication = new Application(siteUser, gpaScore.getGpa(), languageTestScore.getLanguageTest(),
                    term, before.getUpdateCount() + 1, firstChoiceUniversity, secondChoiceUniversity, thirdChoiceUniversity, getRandomNickname());
            newApplication.setVerifyStatus(VerifyStatus.APPROVED);
            applicationRepository.save(newApplication);
        }
        return true;
    }

    private GpaScore getValidGpaScore(SiteUser siteUser, Long gpaScoreId) {
        GpaScore gpaScore = gpaScoreRepository.findGpaScoreBySiteUserAndId(siteUser, gpaScoreId)
                .orElseThrow(() -> new CustomException(INVALID_GPA_SCORE));
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
