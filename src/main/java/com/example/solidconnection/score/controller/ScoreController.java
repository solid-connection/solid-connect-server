package com.example.solidconnection.score.controller;

import com.example.solidconnection.custom.resolver.AuthorizedUser;
import com.example.solidconnection.score.dto.GpaScoreRequest;
import com.example.solidconnection.score.dto.GpaScoreStatusResponse;
import com.example.solidconnection.score.dto.LanguageTestScoreRequest;
import com.example.solidconnection.score.dto.LanguageTestScoreStatusResponse;
import com.example.solidconnection.score.service.ScoreService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/score")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    // 학점을 등록하는 api
    @PostMapping("/gpa")
    public ResponseEntity<Long> submitGpaScore(
            @AuthorizedUser SiteUser siteUser,
            @Valid @RequestBody GpaScoreRequest gpaScoreRequest
    ) {
        Long id = scoreService.submitGpaScore(siteUser, gpaScoreRequest);
        return ResponseEntity.ok(id);
    }

    // 어학성적을 등록하는 api
    @PostMapping("/languageTest")
    public ResponseEntity<Long> submitLanguageTestScore(
            @AuthorizedUser SiteUser siteUser,
            @Valid @RequestBody LanguageTestScoreRequest languageTestScoreRequest
    ) {
        Long id = scoreService.submitLanguageTestScore(siteUser, languageTestScoreRequest);
        return ResponseEntity.ok(id);
    }

    // 학점 상태를 확인하는 api
    @GetMapping("/gpa")
    public ResponseEntity<GpaScoreStatusResponse> getGpaScoreStatus(
            @AuthorizedUser SiteUser siteUser
    ) {
        GpaScoreStatusResponse gpaScoreStatus = scoreService.getGpaScoreStatus(siteUser);
        return ResponseEntity.ok(gpaScoreStatus);
    }

    // 어학 성적 상태를 확인하는 api
    @GetMapping("/languageTest")
    public ResponseEntity<LanguageTestScoreStatusResponse> getLanguageTestScoreStatus(
            @AuthorizedUser SiteUser siteUser
    ) {
        LanguageTestScoreStatusResponse languageTestScoreStatus = scoreService.getLanguageTestScoreStatus(siteUser);
        return ResponseEntity.ok(languageTestScoreStatus);
    }
}
