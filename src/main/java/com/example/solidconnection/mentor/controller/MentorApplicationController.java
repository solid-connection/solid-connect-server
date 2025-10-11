package com.example.solidconnection.mentor.controller;

import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.mentor.dto.MentorApplicationRequest;
import com.example.solidconnection.mentor.service.MentorApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RequestMapping("/mentees")
@RestController
public class MentorApplicationController {

    private final MentorApplicationService mentorApplicationService;

    @PostMapping("/mentor-applications")
    public ResponseEntity<Void> requestMentorApplication(
            @AuthorizedUser long siteUserId,
            @Valid @RequestPart("mentorApplicationRequest") MentorApplicationRequest mentorApplicationRequest,
            @RequestParam("file") MultipartFile file
    ) {
        mentorApplicationService.submitMentorApplication(siteUserId, mentorApplicationRequest, file);
        return ResponseEntity.ok().build();
    }
}
