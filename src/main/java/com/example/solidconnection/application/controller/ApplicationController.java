package com.example.solidconnection.application.controller;

import com.example.solidconnection.application.dto.*;
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
public class ApplicationController implements ApplicationControllerSwagger {

    private final ApplicationSubmissionService applicationSubmissionService;
    private final ApplicationQueryService applicationQueryService;

    // 지원서 제출하기 api
    @PostMapping()
    public ResponseEntity<ApplicationSubmissionResponse> apply(
            Principal principal,
            @Valid @RequestBody ApplyRequest applyRequest) {
        boolean result = applicationSubmissionService.apply(principal.getName(), applyRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApplicationSubmissionResponse(result));
    }

    @GetMapping
    public ResponseEntity<ApplicationsResponse> getApplicants(
            Principal principal,
            @RequestParam(required = false, defaultValue = "") String region,
            @RequestParam(required = false, defaultValue = "") String keyword) {
        applicationQueryService.validateSiteUserCanViewApplicants(principal.getName());
        ApplicationsResponse result = applicationQueryService.getApplicants(principal.getName(), region, keyword);
        return ResponseEntity
                .ok(result);
    }

    @GetMapping("/competitors")
    public ResponseEntity<ApplicationsResponse> getApplicantsForUserCompetitors(
            Principal principal) {
        applicationQueryService.validateSiteUserCanViewApplicants(principal.getName());
        ApplicationsResponse result = applicationQueryService.getApplicantsByUserApplications(principal.getName());
        return ResponseEntity
                .ok(result);
    }
}
