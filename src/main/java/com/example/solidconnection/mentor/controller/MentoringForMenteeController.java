package com.example.solidconnection.mentor.controller;

import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.mentor.dto.CheckMentoringRequest;
import com.example.solidconnection.mentor.dto.CheckedMentoringsResponse;
import com.example.solidconnection.mentor.dto.MatchedMentorResponse;
import com.example.solidconnection.mentor.dto.MentoringApplyRequest;
import com.example.solidconnection.mentor.dto.MentoringApplyResponse;
import com.example.solidconnection.mentor.dto.MentoringForMenteeResponse;
import com.example.solidconnection.mentor.service.MentoringCheckService;
import com.example.solidconnection.mentor.service.MentoringCommandService;
import com.example.solidconnection.mentor.service.MentoringQueryService;
import com.example.solidconnection.security.annotation.RequireRoleAccess;
import com.example.solidconnection.siteuser.domain.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.data.web.SortDefault.SortDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mentee/mentorings")
public class MentoringForMenteeController {

    private final MentoringCommandService mentoringCommandService;
    private final MentoringQueryService mentoringQueryService;
    private final MentoringCheckService mentoringCheckService;

    @RequireRoleAccess(roles = Role.MENTEE)
    @GetMapping("/matched-mentors")
    public ResponseEntity<SliceResponse<MatchedMentorResponse>> getMatchedMentors(
            @AuthorizedUser long siteUserId,
            @PageableDefault
            @SortDefaults({
                    @SortDefault(sort = "confirmedAt", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "id", direction = Sort.Direction.DESC)
            })
            Pageable pageable
    ) {
        SliceResponse<MatchedMentorResponse> response = mentoringQueryService.getMatchedMentors(siteUserId, pageable);
        return ResponseEntity.ok(response);
    }

    @RequireRoleAccess(roles = Role.MENTEE)
    @PostMapping
    public ResponseEntity<MentoringApplyResponse> applyMentoring(
            @AuthorizedUser long siteUserId,
            @Valid @RequestBody MentoringApplyRequest mentoringApplyRequest
    ) {
        MentoringApplyResponse response = mentoringCommandService.applyMentoring(siteUserId, mentoringApplyRequest);
        return ResponseEntity.ok(response);
    }

    @RequireRoleAccess(roles = Role.MENTEE)
    @GetMapping
    public ResponseEntity<SliceResponse<MentoringForMenteeResponse>> getMentorings(
            @AuthorizedUser long siteUserId,
            @RequestParam("verify-status") VerifyStatus verifyStatus,
            @PageableDefault(size = 3)
            @SortDefaults({
                    @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "id", direction = Sort.Direction.DESC)
            })
            Pageable pageable
    ) {
        SliceResponse<MentoringForMenteeResponse> response = mentoringQueryService.getMentoringsForMentee(siteUserId, verifyStatus, pageable);
        return ResponseEntity.ok(response);
    }

    @RequireRoleAccess(roles = {Role.MENTEE})
    @PatchMapping("/check")
    public ResponseEntity<CheckedMentoringsResponse> checkMentorings(
            @AuthorizedUser long siteUserId,
            @Valid @RequestBody CheckMentoringRequest checkMentoringRequest
    ) {
        CheckedMentoringsResponse response = mentoringCheckService.checkMentoringsForMentee(siteUserId, checkMentoringRequest);
        return ResponseEntity.ok(response);
    }
}
