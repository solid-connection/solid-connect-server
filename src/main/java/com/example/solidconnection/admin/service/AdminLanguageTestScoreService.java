package com.example.solidconnection.admin.service;

import static com.example.solidconnection.common.exception.ErrorCode.LANGUAGE_TEST_SCORE_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

import com.example.solidconnection.admin.dto.LanguageTestScoreResponse;
import com.example.solidconnection.admin.dto.LanguageTestScoreSearchResponse;
import com.example.solidconnection.admin.dto.LanguageTestScoreUpdateRequest;
import com.example.solidconnection.admin.dto.ScoreSearchCondition;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.common.discord.DiscordNotificationType;
import com.example.solidconnection.common.discord.DiscordNotifier;
import com.example.solidconnection.common.discord.DiscordReactionEmoji;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminLanguageTestScoreService {

    private final LanguageTestScoreRepository languageTestScoreRepository;
    private final SiteUserRepository siteUserRepository;
    private final DiscordNotifier discordNotifier;

    @Transactional(readOnly = true)
    public Page<LanguageTestScoreSearchResponse> searchLanguageTestScores(ScoreSearchCondition scoreSearchCondition, Pageable pageable) {
        return languageTestScoreRepository.searchLanguageTestScores(scoreSearchCondition, pageable);
    }

    @Transactional
    public LanguageTestScoreResponse updateLanguageTestScore(Long languageTestScoreId, LanguageTestScoreUpdateRequest request) {
        LanguageTestScore languageTestScore = languageTestScoreRepository.findById(languageTestScoreId)
                .orElseThrow(() -> new CustomException(LANGUAGE_TEST_SCORE_NOT_FOUND));
        languageTestScore.updateLanguageTestScore(
                new LanguageTest(
                        request.languageTestType(),
                        request.languageTestScore(),
                        languageTestScore.getLanguageTest().getLanguageTestReportUrl()
                ),
                request.verifyStatus(),
                request.verifyStatus() == VerifyStatus.REJECTED ? request.rejectedReason() : null
        );
        publishReviewResult(languageTestScore, request.verifyStatus());
        return LanguageTestScoreResponse.from(languageTestScore);
    }

    private void publishReviewResult(LanguageTestScore languageTestScore, VerifyStatus verifyStatus) {
        String marker = switch (verifyStatus) {
            case APPROVED -> DiscordReactionEmoji.APPROVED.getValue();
            case REJECTED -> DiscordReactionEmoji.REJECTED.getValue();
            case PENDING -> null;
        };
        if (marker == null) {
            return;
        }
        SiteUser siteUser = siteUserRepository.findById(languageTestScore.getSiteUserId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        discordNotifier.markReviewResult(
                DiscordNotificationType.LANGUAGE_TEST_SCORE,
                languageTestScore.getId(),
                siteUser.getNickname(),
                marker
        );
    }
}
