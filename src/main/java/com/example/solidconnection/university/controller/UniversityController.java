package com.example.solidconnection.university.controller;

import com.example.solidconnection.custom.response.CustomResponse;
import com.example.solidconnection.custom.response.DataResponse;
import com.example.solidconnection.university.dto.LikedResultDto;
import com.example.solidconnection.university.dto.UniversityDetailDto;
import com.example.solidconnection.university.dto.UniversityPreviewDto;
import com.example.solidconnection.university.service.UniversityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/university")
@RestController
public class UniversityController {

    private final UniversityService universityService;

    @GetMapping("/detail/{universityInfoForApplyId}")
    public CustomResponse getUniversityDetails(
            Principal principal,
            @PathVariable Long universityInfoForApplyId) {
        UniversityDetailDto universityDetailDto = universityService.getDetail(universityInfoForApplyId);
        if (principal != null) {
            boolean isLiked = universityService.getIsLiked(principal.getName(), universityInfoForApplyId);
            universityDetailDto.setLiked(isLiked);
        }
        return new DataResponse<>(universityDetailDto);
    }

    @GetMapping("/search")
    public CustomResponse searchUniversity(
            @RequestParam(required = false, defaultValue = "") String region,
            @RequestParam(required = false, defaultValue = "") List<String> keyword,
            @RequestParam(required = false, defaultValue = "") String testType,
            @RequestParam(required = false, defaultValue = "") String testScore) {
        List<UniversityPreviewDto> universityPreviewDto = universityService.search(region, keyword, testType, testScore);
        return new DataResponse<>(universityPreviewDto);
    }

    @PostMapping("/{universityInfoForApplyId}/like")
    public CustomResponse addWishUniversity(
            Principal principal,
            @PathVariable Long universityInfoForApplyId) {
        LikedResultDto likedResultDto = universityService.like(principal.getName(), universityInfoForApplyId);
        return new DataResponse<>(likedResultDto);
    }
}
