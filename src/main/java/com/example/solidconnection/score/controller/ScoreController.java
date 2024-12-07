package com.example.solidconnection.score.controller;

import com.example.solidconnection.score.dto.GpaScoreRequest;
import com.example.solidconnection.score.dto.GpaScoreStatusResponse;
import com.example.solidconnection.score.dto.LanguageTestScoreRequest;
import com.example.solidconnection.score.dto.LanguageTestScoreStatusResponse;
import com.example.solidconnection.score.service.ScoreService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static com.example.solidconnection.config.swagger.SwaggerConfig.ACCESS_TOKEN;

@RestController
@RequestMapping("/score")
@RequiredArgsConstructor
@SecurityRequirements
@SecurityRequirement(name = ACCESS_TOKEN)
public class ScoreController {

    private final ScoreService scoreService;

    // 학점을 등록하는 api
    @PostMapping("/gpa")
    public ResponseEntity<Long> submitGpaScore(
            Principal principal,
            @Valid @RequestBody GpaScoreRequest gpaScoreRequest) {
        Long id = scoreService.submitGpaScore(principal.getName(), gpaScoreRequest);
        return ResponseEntity.ok(id);
    }

    // 어학성적을 등록하는 api
    @PostMapping("/languageTest")
    public ResponseEntity<Long> submitLanguageTestScore(
            Principal principal,
            @Valid @RequestBody LanguageTestScoreRequest languageTestScoreRequest) {
        Long id = scoreService.submitLanguageTestScore(principal.getName(), languageTestScoreRequest);
        return ResponseEntity.ok(id);
    }

    // 학점 상태를 확인하는 api
    @GetMapping("/gpa")
    public ResponseEntity<GpaScoreStatusResponse> getGpaScoreStatus(Principal principal) {
        GpaScoreStatusResponse gpaScoreStatus = scoreService.getGpaScoreStatus(principal.getName());
        return ResponseEntity.ok(gpaScoreStatus);
    }

    // 어학 성적 상태를 확인하는 api
    @GetMapping("/languageTest")
    public ResponseEntity<LanguageTestScoreStatusResponse> getLanguageTestScoreStatus(Principal principal) {
        LanguageTestScoreStatusResponse languageTestScoreStatus = scoreService.getLanguageTestScoreStatus(principal.getName());
        return ResponseEntity.ok(languageTestScoreStatus);
    }
}
