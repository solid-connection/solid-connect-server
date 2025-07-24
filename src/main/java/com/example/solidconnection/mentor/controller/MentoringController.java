package com.example.solidconnection.mentor.controller;

import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.mentor.dto.MentoringApplyRequest;
import com.example.solidconnection.mentor.dto.MentoringApplyResponse;
import com.example.solidconnection.mentor.dto.MentoringCheckResponse;
import com.example.solidconnection.mentor.dto.MentoringConfirmRequest;
import com.example.solidconnection.mentor.dto.MentoringConfirmResponse;
import com.example.solidconnection.mentor.dto.MentoringCountResponse;
import com.example.solidconnection.mentor.dto.MentoringListResponse;
import com.example.solidconnection.mentor.service.MentoringCommandService;
import com.example.solidconnection.mentor.service.MentoringQueryService;
import com.example.solidconnection.security.annotation.RequireRoleAccess;
import com.example.solidconnection.siteuser.domain.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mentorings")
public class MentoringController {

    private final MentoringCommandService mentoringCommandService;
    private final MentoringQueryService mentoringQueryService;

    @RequireRoleAccess(roles = Role.MENTEE)
    @PostMapping("/apply")
    public ResponseEntity<MentoringApplyResponse> applyMentoring(
            @AuthorizedUser long siteUserId,
            @Valid @RequestBody MentoringApplyRequest mentoringApplyRequest
    ) {
        MentoringApplyResponse response = mentoringCommandService.applyMentoring(siteUserId, mentoringApplyRequest);
        return ResponseEntity.ok(response);
    }

    @RequireRoleAccess(roles = {Role.ADMIN, Role.MENTOR})
    @GetMapping("/apply")
    public ResponseEntity<MentoringListResponse> getMentorings(
            @AuthorizedUser long siteUserId
    ) {
        MentoringListResponse responses = mentoringQueryService.getMentorings(siteUserId);
        return ResponseEntity.ok(responses);
    }

    @RequireRoleAccess(roles = {Role.ADMIN, Role.MENTOR})
    @PatchMapping("/{mentoring-id}/apply")
    public ResponseEntity<MentoringConfirmResponse> confirmMentoring(
            @AuthorizedUser long siteUserId,
            @PathVariable("mentoring-id") Long mentoringId,
            @Valid @RequestBody MentoringConfirmRequest mentoringConfirmRequest
    ) {
        MentoringConfirmResponse response = mentoringCommandService.confirmMentoring(siteUserId, mentoringId, mentoringConfirmRequest);
        return ResponseEntity.ok(response);
    }

    @RequireRoleAccess(roles = {Role.ADMIN, Role.MENTOR})
    @PatchMapping("/{mentoring-id}/check")
    public ResponseEntity<MentoringCheckResponse> checkMentoring(
            @AuthorizedUser long siteUserId,
            @PathVariable("mentoring-id") Long mentoringId
    ) {
        MentoringCheckResponse response = mentoringCommandService.checkMentoring(siteUserId, mentoringId);
        return ResponseEntity.ok(response);
    }

    @RequireRoleAccess(roles = {Role.ADMIN, Role.MENTOR})
    @GetMapping("/check")
    public ResponseEntity<MentoringCountResponse> getUncheckedMentoringsCount(
            @AuthorizedUser long siteUserId
    ) {
        MentoringCountResponse response = mentoringQueryService.getNewMentoringsCount(siteUserId);
        return ResponseEntity.ok(response);
    }
}
