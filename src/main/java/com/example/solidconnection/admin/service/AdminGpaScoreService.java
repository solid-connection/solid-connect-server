package com.example.solidconnection.admin.service;

import com.example.solidconnection.admin.dto.GpaScoreResponse;
import com.example.solidconnection.admin.dto.GpaScoreSearchResponse;
import com.example.solidconnection.admin.dto.GpaScoreUpdateRequest;
import com.example.solidconnection.admin.dto.ScoreSearchCondition;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.application.domain.VerifyStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.solidconnection.custom.exception.ErrorCode.GPA_SCORE_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class AdminGpaScoreService {

    private final GpaScoreRepository gpaScoreRepository;

    @Transactional(readOnly = true)
    public Page<GpaScoreSearchResponse> searchGpaScores(ScoreSearchCondition scoreSearchCondition, Pageable pageable) {
        return gpaScoreRepository.searchGpaScores(scoreSearchCondition, pageable);
    }

    @Transactional
    public GpaScoreResponse updateGpaScore(Long gpaScoreId, GpaScoreUpdateRequest request) {
        GpaScore gpaScore = gpaScoreRepository.findById(gpaScoreId)
                .orElseThrow(() -> new CustomException(GPA_SCORE_NOT_FOUND));
        gpaScore.updateGpaScore(
                new Gpa(
                        request.gpa(),
                        request.gpaCriteria(),
                        gpaScore.getGpa().getGpaReportUrl()
                ),
                request.verifyStatus(),
                request.verifyStatus() == VerifyStatus.REJECTED ? request.rejectedReason() : null
        );
        return GpaScoreResponse.from(gpaScore);
    }
}
