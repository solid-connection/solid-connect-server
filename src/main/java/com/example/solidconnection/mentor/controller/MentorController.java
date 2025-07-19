package com.example.solidconnection.mentor.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.mentor.dto.MentorDetailResponse;
import com.example.solidconnection.mentor.dto.MentorPreviewResponse;
import com.example.solidconnection.mentor.service.MentorQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.data.web.SortDefault.SortDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/mentors")
@RestController
public class MentorController {

    private final MentorQueryService mentorQueryService;

    @GetMapping("/{mentor-id}")
    public ResponseEntity<MentorDetailResponse> getMentorDetails(
            @AuthorizedUser long siteUserId,
            @PathVariable("mentor-id") Long mentorId
    ) {
        MentorDetailResponse response = mentorQueryService.getMentorDetails(mentorId, siteUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<SliceResponse<MentorPreviewResponse>> getMentorPreviews(
            @AuthorizedUser long siteUserId,
            @RequestParam("region") String region,
            
            @PageableDefault(size = 3, sort = "menteeCount", direction = DESC)
            @SortDefaults({
                    @SortDefault(sort = "menteeCount", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "id", direction = Sort.Direction.ASC)
            })
            Pageable pageable
    ) {
        SliceResponse<MentorPreviewResponse> response = mentorQueryService.getMentorPreviews(region, siteUserId, pageable);
        return ResponseEntity.ok(response);
    }
}
