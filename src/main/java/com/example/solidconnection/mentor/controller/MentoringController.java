package com.example.solidconnection.mentor.controller;

import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.mentor.dto.MentoringApplyRequest;
import com.example.solidconnection.mentor.dto.MentoringApplyResponse;
import com.example.solidconnection.mentor.service.MentoringCommandService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mentorings")
public class MentoringController {

    private final MentoringCommandService mentoringCommandService;

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
}
