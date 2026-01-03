package com.example.solidconnection.admin.controller;

import com.example.solidconnection.admin.dto.MentorApplicationAssignUniversityRequest;
import com.example.solidconnection.admin.dto.MentorApplicationCountResponse;
import com.example.solidconnection.admin.dto.MentorApplicationRejectRequest;
import com.example.solidconnection.admin.dto.MentorApplicationSearchCondition;
import com.example.solidconnection.admin.dto.MentorApplicationSearchResponse;
import com.example.solidconnection.admin.service.AdminMentorApplicationService;
import com.example.solidconnection.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/admin/mentor-applications")
@RestController
@Slf4j
public class AdminMentorApplicationController {
    private final AdminMentorApplicationService adminMentorApplicationService;

    @GetMapping
    public ResponseEntity<PageResponse<MentorApplicationSearchResponse>> searchMentorApplications(
            @Valid @ModelAttribute MentorApplicationSearchCondition mentorApplicationSearchCondition,
            Pageable pageable
    ) {
        Page<MentorApplicationSearchResponse> page = adminMentorApplicationService.searchMentorApplications(
                mentorApplicationSearchCondition,
                pageable
        );

        return ResponseEntity.ok(PageResponse.of(page));
    }

    @PostMapping("/{mentor-application-id}/approve")
    public ResponseEntity<Void> approveMentorApplication(
            @PathVariable("mentor-application-id") Long mentorApplicationId
    ) {
        adminMentorApplicationService.approveMentorApplication(mentorApplicationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{mentor-application-id}/reject")
    public ResponseEntity<Void> rejectMentorApplication(
            @PathVariable("mentor-application-id") Long mentorApplicationId,
            @Valid @RequestBody MentorApplicationRejectRequest request
    ) {
        adminMentorApplicationService.rejectMentorApplication(mentorApplicationId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    public ResponseEntity<MentorApplicationCountResponse> getMentorApplicationCount() {
        MentorApplicationCountResponse response = adminMentorApplicationService.getMentorApplicationCount();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{mentor-application-id}/assign-university")
    public ResponseEntity<Void> assignUniversity(
            @PathVariable("mentor-application-id") Long mentorApplicationId,
            @Valid @RequestBody MentorApplicationAssignUniversityRequest request
    ) {
        Long universityId = request.universityId();
        adminMentorApplicationService.assignUniversity(mentorApplicationId, universityId);
        return ResponseEntity.ok().build();
    }
}
