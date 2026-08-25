package com.example.solidconnection.admin.service;

import static com.example.solidconnection.common.exception.ErrorCode.LANGUAGE_TEST_SCORE_NOT_FOUND;

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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminLanguageTestScoreService {

    private final LanguageTestScoreRepository languageTestScoreRepository;
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
        publishReaction(languageTestScoreId, request.verifyStatus());
        return LanguageTestScoreResponse.from(languageTestScore);
    }

    private void publishReaction(long languageTestScoreId, VerifyStatus verifyStatus) {
        String emoji = switch (verifyStatus) {
            case APPROVED -> DiscordReactionEmoji.APPROVED.getValue();
            case REJECTED -> DiscordReactionEmoji.REJECTED.getValue();
            case PENDING -> null;
        };
        if (emoji != null) {
            discordNotifier.addReaction(DiscordNotificationType.LANGUAGE_TEST_SCORE, languageTestScoreId, emoji);
        }
    }
}
