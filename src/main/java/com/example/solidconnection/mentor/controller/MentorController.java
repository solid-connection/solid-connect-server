package com.example.solidconnection.mentor.controller;

import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.mentor.dto.MentorDetailResponse;
import com.example.solidconnection.mentor.dto.MentorPreviewsResponse;
import com.example.solidconnection.mentor.service.MentorQueryService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RequiredArgsConstructor
@RequestMapping("/mentors")
@RestController
public class MentorController {

    private final MentorQueryService mentorQueryService;

    @GetMapping("/{mentor-id}")
    public ResponseEntity<MentorDetailResponse> getMentorDetails(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("mentor-id") Long mentorId
    ) {
        MentorDetailResponse response = mentorQueryService.getMentorDetails(mentorId, siteUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<MentorPreviewsResponse> getMentorPreviews(
            @AuthorizedUser SiteUser siteUser,
            @RequestParam("region") String region,
            @PageableDefault(size = 3, sort = "menteeCount", direction = DESC) Pageable pageable
    ) {
        MentorPreviewsResponse response = mentorQueryService.getMentorPreviews(region, siteUser, pageable);
        return ResponseEntity.ok(response);
    }
}
