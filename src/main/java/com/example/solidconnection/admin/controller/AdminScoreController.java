package com.example.solidconnection.admin.controller;

import com.example.solidconnection.admin.dto.GpaScoreResponse;
import com.example.solidconnection.admin.dto.GpaScoreSearchResponse;
import com.example.solidconnection.admin.dto.GpaScoreUpdateRequest;
import com.example.solidconnection.admin.dto.LanguageTestScoreResponse;
import com.example.solidconnection.admin.dto.LanguageTestScoreSearchResponse;
import com.example.solidconnection.admin.dto.LanguageTestScoreUpdateRequest;
import com.example.solidconnection.admin.dto.ScoreSearchCondition;
import com.example.solidconnection.admin.service.AdminGpaScoreService;
import com.example.solidconnection.admin.service.AdminLanguageTestScoreService;
import com.example.solidconnection.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/admin/scores")
@RestController
public class AdminScoreController {

    private final AdminGpaScoreService adminGpaScoreService;
    private final AdminLanguageTestScoreService adminLanguageTestScoreService;

    @GetMapping("/gpas")
    public ResponseEntity<PageResponse<GpaScoreSearchResponse>> searchGpaScores(
            @Valid @ModelAttribute ScoreSearchCondition scoreSearchCondition,
            Pageable pageable
    ) {
        Page<GpaScoreSearchResponse> page = adminGpaScoreService.searchGpaScores(
                scoreSearchCondition,
                pageable
        );
        return ResponseEntity.ok(PageResponse.of(page));
    }

    @PutMapping("/gpas/{gpa-score-id}")
    public ResponseEntity<GpaScoreResponse> updateGpaScore(
            @PathVariable("gpa-score-id") Long gpaScoreId,
            @Valid @RequestBody GpaScoreUpdateRequest request
    ) {
        GpaScoreResponse response = adminGpaScoreService.updateGpaScore(gpaScoreId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/language-tests")
    public ResponseEntity<PageResponse<LanguageTestScoreSearchResponse>> searchLanguageTestScores(
            @Valid @ModelAttribute ScoreSearchCondition scoreSearchCondition,
            Pageable pageable
    ) {
        Page<LanguageTestScoreSearchResponse> page = adminLanguageTestScoreService.searchLanguageTestScores(
                scoreSearchCondition,
                pageable
        );
        return ResponseEntity.ok(PageResponse.of(page));
    }

    @PutMapping("/language-tests/{language-test-score-id}")
    public ResponseEntity<LanguageTestScoreResponse> updateLanguageTestScore(
            @PathVariable("language-test-score-id") Long languageTestScoreId,
            @Valid @RequestBody LanguageTestScoreUpdateRequest request
    ) {
        LanguageTestScoreResponse response = adminLanguageTestScoreService.updateLanguageTestScore(languageTestScoreId, request);
        return ResponseEntity.ok(response);
    }
}
