package com.example.solidconnection.mentor.controller;

import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.mentor.dto.MentorDetailResponse;
import com.example.solidconnection.mentor.service.MentorQueryService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/mentors")
@RestController
public class MentorController {

    private final MentorQueryService mentorQueryService;

    @GetMapping("/{mentor-id}")
    public ResponseEntity<?> getMentorDetails(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("mentor-id") Long mentorId
    ) {
        MentorDetailResponse response = mentorQueryService.getMentorDetails(mentorId, siteUser);
        return ResponseEntity.ok(response);
    }
}
