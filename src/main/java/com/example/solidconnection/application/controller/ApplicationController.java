package com.example.solidconnection.application.controller;

import com.example.solidconnection.application.dto.ApplicationSubmissionResponse;
import com.example.solidconnection.application.dto.ApplicationsResponse;
import com.example.solidconnection.application.dto.ApplyRequest;
import com.example.solidconnection.application.service.ApplicationQueryService;
import com.example.solidconnection.application.service.ApplicationSubmissionService;
import com.example.solidconnection.custom.resolver.AuthorizedUser;
import com.example.solidconnection.siteuser.domain.SiteUser;
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

@RequiredArgsConstructor
@RequestMapping("/applications")
@RestController
public class ApplicationController {

    private final ApplicationSubmissionService applicationSubmissionService;
    private final ApplicationQueryService applicationQueryService;

    // 지원서 제출하기 api
    @PostMapping
    public ResponseEntity<ApplicationSubmissionResponse> apply(
            @AuthorizedUser SiteUser siteUser,
            @Valid @RequestBody ApplyRequest applyRequest
    ) {
        boolean result = applicationSubmissionService.apply(siteUser, applyRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApplicationSubmissionResponse(result));
    }

    @GetMapping
    public ResponseEntity<ApplicationsResponse> getApplicants(
            @AuthorizedUser SiteUser siteUser,
            @RequestParam(required = false, defaultValue = "") String region,
            @RequestParam(required = false, defaultValue = "") String keyword
    ) {
        applicationQueryService.validateSiteUserCanViewApplicants(siteUser);
        ApplicationsResponse result = applicationQueryService.getApplicants(siteUser, region, keyword);
        return ResponseEntity
                .ok(result);
    }

    @GetMapping("/competitors")
    public ResponseEntity<ApplicationsResponse> getApplicantsForUserCompetitors(
            @AuthorizedUser SiteUser siteUser
    ) {
        applicationQueryService.validateSiteUserCanViewApplicants(siteUser);
        ApplicationsResponse result = applicationQueryService.getApplicantsByUserApplications(siteUser);
        return ResponseEntity
                .ok(result);
    }
}
