package com.example.solidconnection.score.repository.custom;

import com.example.solidconnection.admin.dto.GpaScoreSearchResponse;
import com.example.solidconnection.admin.dto.ScoreSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GpaScoreFilterRepository {

    Page<GpaScoreSearchResponse> searchGpaScores(ScoreSearchCondition scoreSearchCondition, Pageable pageable);
}
