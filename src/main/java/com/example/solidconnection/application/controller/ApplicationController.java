package com.example.solidconnection.application.controller;

import com.example.solidconnection.application.dto.ApplicationSubmissionResponse;
import com.example.solidconnection.application.dto.ApplicationsResponse;
import com.example.solidconnection.application.dto.ScoreRequest;
import com.example.solidconnection.application.dto.UniversityChoiceRequest;
import com.example.solidconnection.application.dto.VerifyStatusResponse;
import com.example.solidconnection.application.service.ApplicationQueryService;
import com.example.solidconnection.application.service.ApplicationSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RequiredArgsConstructor
@RequestMapping("/application")
@RestController
public class ApplicationController {

    private final ApplicationSubmissionService applicationSubmissionService;
    private final ApplicationQueryService applicationQueryService;

    @PostMapping("/score")
    public ResponseEntity<ApplicationSubmissionResponse> submitScore(
            Principal principal,
            @Valid @RequestBody ScoreRequest scoreRequest) {
        boolean result = applicationSubmissionService.submitScore(principal.getName(), scoreRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApplicationSubmissionResponse(result));
    }

    @PostMapping("/university")
    public ResponseEntity<ApplicationSubmissionResponse> submitUniversityChoice(
            Principal principal,
            @Valid @RequestBody UniversityChoiceRequest universityChoiceRequest) {
        boolean result = applicationSubmissionService.submitUniversityChoice(principal.getName(), universityChoiceRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApplicationSubmissionResponse(result));
    }

    @GetMapping
    public ResponseEntity<ApplicationsResponse> getApplicants(
            Principal principal,
            @RequestParam(required = false, defaultValue = "") String region,
            @RequestParam(required = false, defaultValue = "") String keyword) {
        ApplicationsResponse result = applicationQueryService.getApplicants(principal.getName(), region, keyword);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    public ResponseEntity<VerifyStatusResponse> getApplicationVerifyStatus(Principal principal) {
        VerifyStatusResponse result = applicationQueryService.getVerifyStatus(principal.getName());
        return ResponseEntity.ok(result);
    }
}
