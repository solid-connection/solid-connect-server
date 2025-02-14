package com.example.solidconnection.admin.controller;

import com.example.solidconnection.admin.dto.GpaScoreSearchResponse;
import com.example.solidconnection.admin.dto.GpaScoreVerificationResponse;
import com.example.solidconnection.admin.dto.GpaScoreVerifyRequest;
import com.example.solidconnection.admin.dto.ScoreSearchCondition;
import com.example.solidconnection.admin.service.ScoreVerificationAdminService;
import com.example.solidconnection.custom.response.PageResponse;
import com.example.solidconnection.util.PagingUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/admin/scores")
@RestController
public class ScoreVerificationAdminController {

    private final ScoreVerificationAdminService scoreVerificationAdminService;

    @GetMapping("/gpas")
    public ResponseEntity<PageResponse<GpaScoreSearchResponse>> searchGpaScores(
            @Valid @ModelAttribute ScoreSearchCondition scoreSearchCondition,
            Pageable pageable
    ) {
        PagingUtils.validatePage(pageable.getPageNumber(), pageable.getPageSize());
        Page<GpaScoreSearchResponse> page = scoreVerificationAdminService.searchGpaScores(scoreSearchCondition, pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }

    @PatchMapping("/gpas/{gpa_score_id}/verify")
    public ResponseEntity<GpaScoreVerificationResponse> verifyGpaScore(
            @PathVariable("gpa_score_id") Long gpaScoreId,
            @Valid @RequestBody GpaScoreVerifyRequest gpaScoreVerifyRequest
    ) {
        GpaScoreVerificationResponse response = scoreVerificationAdminService.verifyGpaScore(gpaScoreId, gpaScoreVerifyRequest);
        return ResponseEntity.ok(response);
    }
}
