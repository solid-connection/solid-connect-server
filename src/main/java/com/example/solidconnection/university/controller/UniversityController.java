package com.example.solidconnection.university.controller;

import com.example.solidconnection.custom.resolver.AuthorizedUser;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.service.SiteUserService;
import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.university.dto.IsLikeResponse;
import com.example.solidconnection.university.dto.LikeResultResponse;
import com.example.solidconnection.university.dto.UniversityDetailResponse;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponse;
import com.example.solidconnection.university.dto.UniversityRecommendsResponse;
import com.example.solidconnection.university.service.UniversityLikeService;
import com.example.solidconnection.university.service.UniversityQueryService;
import com.example.solidconnection.university.service.UniversityRecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/universities")
@RestController
public class UniversityController {

    private final UniversityQueryService universityQueryService;
    private final UniversityLikeService universityLikeService;
    private final UniversityRecommendService universityRecommendService;
    private final SiteUserService siteUserService;

    @GetMapping("/recommend")
    public ResponseEntity<UniversityRecommendsResponse> getUniversityRecommends(
            @AuthorizedUser SiteUser siteUser
    ) {
        if (siteUser == null) {
            return ResponseEntity.ok(universityRecommendService.getGeneralRecommends());
        } else {
            return ResponseEntity.ok(universityRecommendService.getPersonalRecommends(siteUser));
        }
    }

    @GetMapping("/like")
    public ResponseEntity<List<UniversityInfoForApplyPreviewResponse>> getMyWishUniversity(
            @AuthorizedUser SiteUser siteUser
    ) {
        List<UniversityInfoForApplyPreviewResponse> wishUniversities = siteUserService.getWishUniversity(siteUser);
        return ResponseEntity.ok(wishUniversities);
    }

    @GetMapping("/{universityInfoForApplyId}/like")
    public ResponseEntity<IsLikeResponse> getIsLiked(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable Long universityInfoForApplyId
    ) {
        IsLikeResponse isLiked = universityLikeService.getIsLiked(siteUser, universityInfoForApplyId);
        return ResponseEntity.ok(isLiked);
    }

    @PostMapping("/{universityInfoForApplyId}/like")
    public ResponseEntity<LikeResultResponse> addWishUniversity(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable Long universityInfoForApplyId
    ) {
        LikeResultResponse likeResultResponse = universityLikeService.likeUniversity(siteUser, universityInfoForApplyId);
        return ResponseEntity.ok(likeResultResponse);
    }

    @DeleteMapping("/{universityInfoForApplyId}/like")
    public ResponseEntity<LikeResultResponse> cancelWishUniversity(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable Long universityInfoForApplyId
    ) {
        LikeResultResponse likeResultResponse = universityLikeService.cancelLikeUniversity(siteUser, universityInfoForApplyId);
        return ResponseEntity.ok(likeResultResponse);
    }

    @GetMapping("/{universityInfoForApplyId}")
    public ResponseEntity<UniversityDetailResponse> getUniversityDetails(
            @PathVariable Long universityInfoForApplyId
    ) {
        UniversityDetailResponse universityDetailResponse = universityQueryService.getUniversityDetail(universityInfoForApplyId);
        return ResponseEntity.ok(universityDetailResponse);
    }

    // todo return타입 UniversityInfoForApplyPreviewResponses로 추후 수정 필요
    @GetMapping("/search")
    public ResponseEntity<List<UniversityInfoForApplyPreviewResponse>> searchUniversity(
            @RequestParam(required = false, defaultValue = "") String region,
            @RequestParam(required = false, defaultValue = "") List<String> keyword,
            @RequestParam(required = false, defaultValue = "") LanguageTestType testType,
            @RequestParam(required = false, defaultValue = "") String testScore
    ) {
        List<UniversityInfoForApplyPreviewResponse> universityInfoForApplyPreviewResponse
                = universityQueryService.searchUniversity(region, keyword, testType, testScore).universityInfoForApplyPreviewResponses();
        return ResponseEntity.ok(universityInfoForApplyPreviewResponse);
    }
}
