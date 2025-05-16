package com.example.solidconnection.admin.service;

import com.example.solidconnection.admin.dto.LanguageTestScoreResponse;
import com.example.solidconnection.admin.dto.LanguageTestScoreSearchResponse;
import com.example.solidconnection.admin.dto.LanguageTestScoreUpdateRequest;
import com.example.solidconnection.admin.dto.ScoreSearchCondition;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.application.domain.VerifyStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.solidconnection.common.exception.ErrorCode.LANGUAGE_TEST_SCORE_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class AdminLanguageTestScoreService {

    private final LanguageTestScoreRepository languageTestScoreRepository;

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
        return LanguageTestScoreResponse.from(languageTestScore);
    }
}
