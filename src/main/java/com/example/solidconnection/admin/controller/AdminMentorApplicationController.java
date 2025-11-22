package com.example.solidconnection.admin.controller;

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
}
