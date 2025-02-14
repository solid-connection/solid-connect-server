package com.example.solidconnection.admin.service;

import com.example.solidconnection.admin.dto.GpaScoreSearchResponse;
import com.example.solidconnection.admin.dto.GpaScoreVerificationResponse;
import com.example.solidconnection.admin.dto.GpaScoreVerifyRequest;
import com.example.solidconnection.admin.dto.GpaUpdateRequest;
import com.example.solidconnection.admin.dto.GpaUpdateResponse;
import com.example.solidconnection.admin.dto.ScoreSearchCondition;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_GPA_SCORE;

@RequiredArgsConstructor
@Service
public class ScoreVerificationAdminService {

    private final GpaScoreRepository gpaScoreRepository;

    @Transactional(readOnly = true)
    public Page<GpaScoreSearchResponse> searchGpaScores(ScoreSearchCondition scoreSearchCondition, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber() - 1,
                pageable.getPageSize()
        );
        return gpaScoreRepository.searchGpaScores(scoreSearchCondition, sortedPageable);
    }

    @Transactional
    public GpaUpdateResponse updateGpa(Long gpaScoreId, GpaUpdateRequest request) {
        GpaScore gpaScore = gpaScoreRepository.findById(gpaScoreId)
                .orElseThrow(() -> new CustomException(INVALID_GPA_SCORE));
        gpaScore.updateGpa(new Gpa(
                request.gpa(),
                request.gpaCriteria(),
                gpaScore.getGpa().getGpaReportUrl()
        ));
        return GpaUpdateResponse.of(gpaScore);
    }

    @Transactional
    public GpaScoreVerificationResponse verifyGpaScore(Long gpaScoreId, GpaScoreVerifyRequest request) {
        GpaScore gpaScore = gpaScoreRepository.findById(gpaScoreId)
                .orElseThrow(() -> new CustomException(INVALID_GPA_SCORE));
        gpaScore.updateGpaScore(request.verifyStatus(), request.rejectedReason());
        return GpaScoreVerificationResponse.of(gpaScore);
    }
}
