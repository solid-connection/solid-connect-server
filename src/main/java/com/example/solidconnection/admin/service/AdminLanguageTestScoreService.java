package com.example.solidconnection.admin.service;

import com.example.solidconnection.admin.dto.LanguageTestScoreSearchResponse;
import com.example.solidconnection.admin.dto.ScoreSearchCondition;
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

    @Transactional(readOnly = true)
    public Page<LanguageTestScoreSearchResponse> searchLanguageTestScores(ScoreSearchCondition scoreSearchCondition, Pageable pageable) {
        return languageTestScoreRepository.searchLanguageTestScores(scoreSearchCondition, pageable);
    }
}
