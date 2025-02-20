package com.example.solidconnection.score.repository.custom;

import com.example.solidconnection.admin.dto.LanguageTestScoreSearchResponse;
import com.example.solidconnection.admin.dto.ScoreSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LanguageTestScoreFilterRepository {

    Page<LanguageTestScoreSearchResponse> searchLanguageTestScores(ScoreSearchCondition scoreSearchCondition, Pageable pageable);
}
