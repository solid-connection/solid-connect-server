package com.example.solidconnection.mentor.controller;

import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.mentor.dto.MentoringApplyRequest;
import com.example.solidconnection.mentor.dto.MentoringApplyResponse;
import com.example.solidconnection.mentor.dto.MentoringConfirmRequest;
import com.example.solidconnection.mentor.dto.MentoringConfirmResponse;
import com.example.solidconnection.mentor.dto.MentoringResponse;
import com.example.solidconnection.mentor.service.MentoringCommandService;
import com.example.solidconnection.mentor.service.MentoringQueryService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mentorings")
public class MentoringController {

    private final MentoringCommandService mentoringCommandService;
    private final MentoringQueryService mentoringQueryService;

    @PostMapping("/apply")
    public ResponseEntity<MentoringApplyResponse> applyMentoring(
            @AuthorizedUser SiteUser siteUser,
            @Valid @RequestBody MentoringApplyRequest mentoringApplyRequest
    ) {
        MentoringApplyResponse response = mentoringCommandService.applyMentoring(siteUser.getId(), mentoringApplyRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // TODO: RequireRoleAccess 어노테이션 추가 필요
    @GetMapping("/apply")
    public ResponseEntity<List<MentoringResponse>> getMentorings(
            @AuthorizedUser SiteUser siteUser
    ) {
        List<MentoringResponse> responses = mentoringQueryService.getMentorings(siteUser.getId());
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{mentoring-id}/apply")
    public ResponseEntity<MentoringConfirmResponse> confirmMentoring(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("mentoring-id") Long mentoringId,
            @Valid @RequestBody MentoringConfirmRequest mentoringConfirmRequest
    ) {
        MentoringConfirmResponse response = mentoringCommandService.confirmMentoring(siteUser.getId(), mentoringId, mentoringConfirmRequest);
        return ResponseEntity.ok(response);
    }
}
