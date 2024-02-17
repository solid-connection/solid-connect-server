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

@RestController
@RequestMapping("/university")
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityService universityService;

    @GetMapping("/detail/{universityInfoForApplyId}")
    public CustomResponse getDetails(Principal principal, @PathVariable Long universityInfoForApplyId) {
        UniversityDetailDto universityDetailDto = universityService.getDetail(universityInfoForApplyId);
        if (principal != null) {
            boolean isLiked = universityService.getIsLiked(principal.getName(), universityInfoForApplyId);
            universityDetailDto.setLiked(isLiked);
        }
        return new DataResponse<>(universityDetailDto);
    }

    @GetMapping("/search")
    public CustomResponse search(@RequestParam(required = false, defaultValue = "") String region,
                                 @RequestParam(required = false, defaultValue = "") String keyword){
        List<UniversityPreviewDto> universityPreviewDto = universityService.search(region, keyword);
        return new DataResponse<>(universityPreviewDto);
    }

    @PostMapping("/{universityInfoForApplyId}/like")
    public CustomResponse like(Principal principal, @PathVariable Long universityInfoForApplyId){
        LikedResultDto likedResultDto = universityService.like(principal.getName(), universityInfoForApplyId);
        return new DataResponse<>(likedResultDto);
    }
}
