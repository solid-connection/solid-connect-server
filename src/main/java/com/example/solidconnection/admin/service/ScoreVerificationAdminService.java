package com.example.solidconnection.admin.service;

import com.example.solidconnection.admin.dto.GpaScoreSearchResponse;
import com.example.solidconnection.admin.dto.ScoreSearchCondition;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
