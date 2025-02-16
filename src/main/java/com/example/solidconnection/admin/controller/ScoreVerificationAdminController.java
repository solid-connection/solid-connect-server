package com.example.solidconnection.admin.controller;

import com.example.solidconnection.admin.dto.GpaScoreResponse;
import com.example.solidconnection.admin.dto.GpaScoreSearchResponse;
import com.example.solidconnection.admin.dto.GpaScoreUpdateRequest;
import com.example.solidconnection.admin.dto.ScoreSearchCondition;
import com.example.solidconnection.admin.service.GpaScoreVerificationAdminService;
import com.example.solidconnection.custom.response.PageResponse;
import com.example.solidconnection.util.PagingUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    private final GpaScoreVerificationAdminService gpaScoreVerificationAdminService;

    @GetMapping("/gpas")
    public ResponseEntity<PageResponse<GpaScoreSearchResponse>> searchGpaScores(
            @Valid @ModelAttribute ScoreSearchCondition scoreSearchCondition,
            @PageableDefault(page = 1) Pageable pageable
    ) {
        PagingUtils.validatePage(pageable.getPageNumber(), pageable.getPageSize());
        Pageable internalPageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize());
        Page<GpaScoreSearchResponse> page = gpaScoreVerificationAdminService.searchGpaScores(scoreSearchCondition, internalPageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }

    @PatchMapping("/gpas/{gpa_score_id}")
    public ResponseEntity<GpaScoreResponse> updateGpaScore(
            @PathVariable("gpa_score_id") Long gpaScoreId,
            @Valid @RequestBody GpaScoreUpdateRequest request
    ) {
        GpaScoreResponse response = gpaScoreVerificationAdminService.updateGpaScore(gpaScoreId, request);
        return ResponseEntity.ok(response);
    }
}
