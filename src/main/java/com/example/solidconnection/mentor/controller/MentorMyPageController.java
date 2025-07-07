package com.example.solidconnection.mentor.controller;

import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.mentor.dto.MentorMyPageResponse;
import com.example.solidconnection.mentor.dto.MentorMyPageUpdateRequest;
import com.example.solidconnection.mentor.service.MentorMyPageService;
import com.example.solidconnection.security.annotation.RequireRoleAccess;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RequestMapping("/mentor/my")
@RestController
public class MentorMyPageController {

    private final MentorMyPageService mentorMyPageService;

    @RequireRoleAccess(roles = {Role.MENTOR})
    @GetMapping
    public ResponseEntity<MentorMyPageResponse> getMentorMyPage(
            @AuthorizedUser SiteUser siteUser
    ) {
        MentorMyPageResponse mentorMyPageResponse = mentorMyPageService.getMentorMyPage(siteUser);
        return ResponseEntity.ok(mentorMyPageResponse);
    }

    @RequireRoleAccess(roles = Role.MENTOR)
    @PutMapping
    public ResponseEntity<String> updateMentorMyPage(
            @AuthorizedUser SiteUser siteUser,
            @Valid MentorMyPageUpdateRequest mentorMyPageUpdateRequest,
            @RequestParam(value = "file", required = false) MultipartFile imageFile
    ) {
        mentorMyPageService.updateMentorMyPage(siteUser, mentorMyPageUpdateRequest, imageFile);
        return ResponseEntity.ok().build();
    }
}
