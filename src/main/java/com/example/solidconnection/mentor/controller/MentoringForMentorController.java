package com.example.solidconnection.mentor.controller;

import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.mentor.dto.CheckMentoringRequest;
import com.example.solidconnection.mentor.dto.CheckedMentoringsResponse;
import com.example.solidconnection.mentor.dto.MentoringConfirmRequest;
import com.example.solidconnection.mentor.dto.MentoringConfirmResponse;
import com.example.solidconnection.mentor.dto.MentoringCountResponse;
import com.example.solidconnection.mentor.dto.MentoringForMentorResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mentor/mentorings")
public class MentoringForMentorController {

    private final MentoringCommandService mentoringCommandService;
    private final MentoringQueryService mentoringQueryService;
    private final MentoringCheckService mentoringCheckService;

    @RequireRoleAccess(roles = {Role.ADMIN, Role.MENTOR})
    @GetMapping
    public ResponseEntity<SliceResponse<MentoringForMentorResponse>> getMentorings(
            @AuthorizedUser long siteUserId,
            @PageableDefault(size = 3)
            @SortDefaults({
                    @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "id", direction = Sort.Direction.DESC)
            })
            Pageable pageable
    ) {
        SliceResponse<MentoringForMentorResponse> response = mentoringQueryService.getMentoringsForMentor(siteUserId, pageable);
        return ResponseEntity.ok(response);
    }

    @RequireRoleAccess(roles = {Role.ADMIN, Role.MENTOR})
    @PatchMapping("/{mentoring-id}")
    public ResponseEntity<MentoringConfirmResponse> confirmMentoring(
            @AuthorizedUser long siteUserId,
            @PathVariable("mentoring-id") Long mentoringId,
            @Valid @RequestBody MentoringConfirmRequest mentoringConfirmRequest
    ) {
        MentoringConfirmResponse response = mentoringCommandService.confirmMentoring(siteUserId, mentoringId, mentoringConfirmRequest);
        return ResponseEntity.ok(response);
    }

    @RequireRoleAccess(roles = {Role.ADMIN, Role.MENTOR})
    @PatchMapping("/check")
    public ResponseEntity<CheckedMentoringsResponse> checkMentoring(
            @AuthorizedUser long siteUserId,
            @RequestBody CheckMentoringRequest mentoringCheckRequest
    ) {
        CheckedMentoringsResponse response = mentoringCheckService.checkMentoringsForMentor(siteUserId, mentoringCheckRequest);
        return ResponseEntity.ok(response);
    }

    @RequireRoleAccess(roles = {Role.ADMIN, Role.MENTOR})
    @GetMapping("/check")
    public ResponseEntity<MentoringCountResponse> getUncheckedMentoringsCount(
            @AuthorizedUser long siteUserId
    ) {
        MentoringCountResponse response = mentoringCheckService.getUncheckedMentoringCount(siteUserId);
        return ResponseEntity.ok(response);
    }
}
