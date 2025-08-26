package com.example.solidconnection.application.controller;

import com.example.solidconnection.application.dto.ApplicationSubmissionResponse;
import com.example.solidconnection.application.dto.ApplicationsResponse;
import com.example.solidconnection.application.dto.ApplyRequest;
import com.example.solidconnection.application.service.ApplicationQueryService;
import com.example.solidconnection.application.service.ApplicationSubmissionService;
import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.security.annotation.RequireRoleAccess;
import com.example.solidconnection.siteuser.domain.Role;
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
            @AuthorizedUser long siteUserId,
            @Valid @RequestBody ApplyRequest applyRequest
    ) {
        ApplicationSubmissionResponse applicationSubmissionResponse = applicationSubmissionService.apply(siteUserId, applyRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(applicationSubmissionResponse);
    }

    // @RequireRoleAccess(roles = {Role.ADMIN}) // todo : 추후 어드민 페이지에서 권한 변경 기능 추가 필요
    @GetMapping
    public ResponseEntity<ApplicationsResponse> getApplicants(
            @AuthorizedUser long siteUserId,
            @RequestParam(required = false, defaultValue = "") String region,
            @RequestParam(required = false, defaultValue = "") String keyword
    ) {
        applicationQueryService.validateSiteUserCanViewApplicants(siteUserId);
        ApplicationsResponse result = applicationQueryService.getApplicants(siteUserId, region, keyword);
        return ResponseEntity
                .ok(result);
    }

    @GetMapping("/competitors")
    public ResponseEntity<ApplicationsResponse> getApplicantsForUserCompetitors(
            @AuthorizedUser long siteUserId
    ) {
        applicationQueryService.validateSiteUserCanViewApplicants(siteUserId);
        ApplicationsResponse result = applicationQueryService.getApplicantsByUserApplications(siteUserId);
        return ResponseEntity
                .ok(result);
    }
}
