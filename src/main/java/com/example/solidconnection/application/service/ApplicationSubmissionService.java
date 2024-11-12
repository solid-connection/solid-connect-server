package com.example.solidconnection.application.service;

import com.example.solidconnection.application.domain.*;
import com.example.solidconnection.application.dto.ApplyRequest;
import com.example.solidconnection.application.dto.UniversityChoiceRequest;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.VerifyStatus;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.example.solidconnection.custom.exception.ErrorCode.*;

@RequiredArgsConstructor
@Service
public class ApplicationSubmissionService {

    public static final int APPLICATION_UPDATE_COUNT_LIMIT = 3;

    private final ApplicationRepository applicationRepository;
    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;
    private final SiteUserRepository siteUserRepository;
    private final GpaScoreRepository gpaScoreRepository;
    private final LanguageTestScoreRepository languageTestScoreRepository;

    @Value("${university.term}")
    private String term;

    // 학점 및 어학성적이 모두 유효한 경우에만 지원서 등록이 가능하다.
    // 기존에 있던 status field 우선 APRROVED로 입력시킨다.
    @Transactional
    public boolean apply(String email, ApplyRequest applyRequest) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        UniversityChoiceRequest universityChoiceRequest = applyRequest.universityChoiceRequest();
        validateUniversityChoices(universityChoiceRequest);

        Long gpaScoreId = applyRequest.gpaScoreId();
        Long languageTestScoreId = applyRequest.languageTestScoreId();
        GpaScore gpaScore = validateGpaScore(siteUser, gpaScoreId);
        LanguageTestScore languageTestScore = validateLanguageTestScore(siteUser, languageTestScoreId);

        Optional<Application> application = applicationRepository.findBySiteUserAndTerm(siteUser, term);
        application.ifPresentOrElse(before -> {
            validateUpdateLimitNotExceed(before);
            UniversityInfoForApply firstChoiceUniversity = universityInfoForApplyRepository
                    .getUniversityInfoForApplyByIdAndTerm(universityChoiceRequest.firstChoiceUniversityId(), term);
            UniversityInfoForApply secondChoiceUniversity = Optional.ofNullable(universityChoiceRequest.secondChoiceUniversityId())
                    .map(id -> universityInfoForApplyRepository.getUniversityInfoForApplyByIdAndTerm(id, term))
                    .orElse(null);
            UniversityInfoForApply thirdChoiceUniversity = Optional.ofNullable(universityChoiceRequest.thirdChoiceUniversityId())
                    .map(id -> universityInfoForApplyRepository.getUniversityInfoForApplyByIdAndTerm(id, term))
                    .orElse(null);
            before.updateApplication(gpaScore.getGpa(), languageTestScore.getLanguageTest(), firstChoiceUniversity, secondChoiceUniversity, thirdChoiceUniversity, getRandomNickname());
            applicationRepository.save(before);
        }, () -> {
            Application newApplication = new Application(siteUser, gpaScore.getGpa(), languageTestScore.getLanguageTest(), term);
            newApplication.setVerifyStatus(VerifyStatus.APPROVED);
            applicationRepository.save(newApplication);
        });
        return true;
    }

    private GpaScore validateGpaScore(SiteUser siteUser, Long gpaScoreId) {
        GpaScore gpaScore = gpaScoreRepository.findGpaScoreBySiteUserAndId(siteUser, gpaScoreId)
                .orElseThrow(() -> new CustomException(INVALID_GPA_SCORE));
        if (gpaScore.getVerifyStatus() != VerifyStatus.APPROVED) {
            throw new CustomException(INVALID_GPA_SCORE_STATUS);
        }
        return gpaScore;
    }

    private LanguageTestScore validateLanguageTestScore(SiteUser siteUser, Long languageTestScoreId) {
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

    // 입력값 유효성 검증
    private void validateUniversityChoices(UniversityChoiceRequest universityChoiceRequest) {
        Set<Long> uniqueUniversityIds = new HashSet<>();
        uniqueUniversityIds.add(universityChoiceRequest.firstChoiceUniversityId());
        if (universityChoiceRequest.secondChoiceUniversityId() != null) {
            addUniversityChoice(uniqueUniversityIds, universityChoiceRequest.secondChoiceUniversityId());
        }
        if (universityChoiceRequest.thirdChoiceUniversityId() != null) {
            addUniversityChoice(uniqueUniversityIds, universityChoiceRequest.thirdChoiceUniversityId());
        }
    }

    private void addUniversityChoice(Set<Long> uniqueUniversityIds, Long universityId) {
        boolean notAdded = !uniqueUniversityIds.add(universityId);
        if (notAdded) {
            throw new CustomException(CANT_APPLY_FOR_SAME_UNIVERSITY);
        }
    }
}
