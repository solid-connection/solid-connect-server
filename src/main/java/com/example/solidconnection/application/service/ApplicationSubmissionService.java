package com.example.solidconnection.application.service;

import static com.example.solidconnection.common.exception.ErrorCode.APPLY_UPDATE_LIMIT_EXCEED;
import static com.example.solidconnection.common.exception.ErrorCode.CHOICE_COUNT_EXCEEDS_LIMIT;
import static com.example.solidconnection.common.exception.ErrorCode.CURRENT_TERM_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.GPA_SCORE_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_GPA_SCORE_STATUS;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_LANGUAGE_TEST_SCORE;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_LANGUAGE_TEST_SCORE_STATUS;
import static com.example.solidconnection.common.exception.ErrorCode.UNIV_APPLY_INFO_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.ApplicationChoice;
import com.example.solidconnection.application.dto.ApplicationSubmissionResponse;
import com.example.solidconnection.application.dto.ApplyRequest;
import com.example.solidconnection.application.dto.UnivApplyInfoChoiceRequest;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.term.domain.Term;
import com.example.solidconnection.term.repository.TermRepository;
import com.example.solidconnection.university.domain.HomeUniversity;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.repository.HomeUniversityRepository;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ApplicationSubmissionService {

    public static final int APPLICATION_UPDATE_COUNT_LIMIT = 3;
    private final ApplicationRepository applicationRepository;
    private final GpaScoreRepository gpaScoreRepository;
    private final LanguageTestScoreRepository languageTestScoreRepository;
    private final SiteUserRepository siteUserRepository;
    private final TermRepository termRepository;
    private final UnivApplyInfoRepository univApplyInfoRepository;
    private final HomeUniversityRepository homeUniversityRepository;

    @Transactional
    public ApplicationSubmissionResponse apply(long siteUserId, ApplyRequest applyRequest) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        UnivApplyInfoChoiceRequest choiceRequest = applyRequest.univApplyInfoChoiceRequest();
        GpaScore gpaScore = getValidGpaScore(siteUser, applyRequest.gpaScoreId());
        LanguageTestScore languageTestScore = getValidLanguageTestScore(siteUser, applyRequest.languageTestScoreId());
        Term term = termRepository.findByIsCurrentTrue()
                .orElseThrow(() -> new CustomException(CURRENT_TERM_NOT_FOUND));

        int maxChoiceCount = resolveMaxChoiceCount(siteUser);
        validateChoiceCount(choiceRequest, maxChoiceCount);

        List<UnivApplyInfo> univApplyInfos = getValidUnivApplyInfos(choiceRequest.univApplyInfoIds());
        List<ApplicationChoice> choices = buildChoices(choiceRequest.univApplyInfoIds());

        Optional<Application> existingApplication =
                applicationRepository.findTopBySiteUserIdAndTermIdAndIsDeleteFalseOrderByIdDesc(siteUser.getId(), term.getId());
        int updateCount = existingApplication
                .map(application -> {
                    validateUpdateLimitNotExceed(application);
                    application.setIsDeleteTrue();
                    return application.getUpdateCount() + 1;
                })
                .orElse(1);

        Application newApplication = new Application(
                siteUser,
                gpaScore.getGpa(),
                languageTestScore.getLanguageTest(),
                term.getId(),
                updateCount,
                choices,
                getRandomNickname()
        );

        newApplication.setVerifyStatus(VerifyStatus.APPROVED);
        applicationRepository.save(newApplication);

        return ApplicationSubmissionResponse.of(APPLICATION_UPDATE_COUNT_LIMIT, newApplication, univApplyInfos);
    }

    private GpaScore getValidGpaScore(SiteUser siteUser, Long gpaScoreId) {
        GpaScore gpaScore = gpaScoreRepository.findGpaScoreBySiteUserIdAndId(siteUser.getId(), gpaScoreId)
                .orElseThrow(() -> new CustomException(GPA_SCORE_NOT_FOUND));
        if (gpaScore.getVerifyStatus() != VerifyStatus.APPROVED) {
            throw new CustomException(INVALID_GPA_SCORE_STATUS);
        }
        return gpaScore;
    }

    private LanguageTestScore getValidLanguageTestScore(SiteUser siteUser, Long languageTestScoreId) {
        LanguageTestScore languageTestScore = languageTestScoreRepository
                .findLanguageTestScoreBySiteUserIdAndId(siteUser.getId(), languageTestScoreId)
                .orElseThrow(() -> new CustomException(INVALID_LANGUAGE_TEST_SCORE));
        if (languageTestScore.getVerifyStatus() != VerifyStatus.APPROVED) {
            throw new CustomException(INVALID_LANGUAGE_TEST_SCORE_STATUS);
        }
        return languageTestScore;
    }

    private int resolveMaxChoiceCount(SiteUser siteUser) {
        if (siteUser.getHomeUniversityId() == null) {
            return HomeUniversity.DEFAULT_MAX_CHOICE_COUNT;
        }
        return homeUniversityRepository.findById(siteUser.getHomeUniversityId())
                .map(HomeUniversity::getMaxChoiceCount)
                .orElse(HomeUniversity.DEFAULT_MAX_CHOICE_COUNT);
    }

    private List<UnivApplyInfo> getValidUnivApplyInfos(List<Long> ids) {
        List<UnivApplyInfo> univApplyInfos = univApplyInfoRepository.findAllByIds(ids);
        if (univApplyInfos.size() != ids.size()) {
            throw new CustomException(UNIV_APPLY_INFO_NOT_FOUND);
        }
        return univApplyInfos;
    }

    private void validateChoiceCount(UnivApplyInfoChoiceRequest request, int maxChoiceCount) {
        if (request.univApplyInfoIds().size() > maxChoiceCount) {
            throw new CustomException(CHOICE_COUNT_EXCEEDS_LIMIT);
        }
    }

    private List<ApplicationChoice> buildChoices(List<Long> univApplyInfoIds) {
        return IntStream.range(0, univApplyInfoIds.size())
                .mapToObj(i -> new ApplicationChoice(i + 1, univApplyInfoIds.get(i)))
                .toList();
    }

    private void validateUpdateLimitNotExceed(Application application) {
        if (application.getUpdateCount() >= APPLICATION_UPDATE_COUNT_LIMIT) {
            throw new CustomException(APPLY_UPDATE_LIMIT_EXCEED);
        }
    }

    private String getRandomNickname() {
        String randomNickname = NicknameCreator.createRandomNickname();
        while (applicationRepository.existsByNicknameForApply(randomNickname)) {
            randomNickname = NicknameCreator.createRandomNickname();
        }
        return randomNickname;
    }
}
