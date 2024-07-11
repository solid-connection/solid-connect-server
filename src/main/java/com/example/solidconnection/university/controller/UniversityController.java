package com.example.solidconnection.university.controller;

import com.example.solidconnection.siteuser.service.SiteUserService;
import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.university.dto.IsLikeResponse;
import com.example.solidconnection.university.dto.LikeResultResponse;
import com.example.solidconnection.university.dto.UniversityDetailResponse;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponse;
import com.example.solidconnection.university.dto.UniversityRecommendsResponse;
import com.example.solidconnection.university.service.UniversityRecommendService;
import com.example.solidconnection.university.service.UniversityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/university")
@RestController
public class UniversityController implements UniversityControllerSwagger {

    private final UniversityService universityService;
    private final UniversityRecommendService universityRecommendService;
    private final SiteUserService siteUserService;

    //todo: uri 를 "/home" 에서 "/university/recommends" 로 변경하고, 위백님께 알리기
    @GetMapping("/recommends")
    public ResponseEntity<UniversityRecommendsResponse> getUniversityRecommends(
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(universityRecommendService.getGeneralRecommends());
        } else {
            return ResponseEntity.ok(universityRecommendService.getPersonalRecommends(principal.getName()));
        }
    }

    @GetMapping("/like") // todo: uri 를 get university/like로 바꾸고 위백님께 알리기
    public ResponseEntity<List<UniversityInfoForApplyPreviewResponse>> getMyWishUniversity(Principal principal) {
        List<UniversityInfoForApplyPreviewResponse> wishUniversities
                = siteUserService.getWishUniversity(principal.getName());
        return ResponseEntity
                .ok(wishUniversities);
    }

    @GetMapping("/{universityInfoForApplyId}/like")
    public ResponseEntity<IsLikeResponse> getIsLiked(
            Principal principal,
            @PathVariable Long universityInfoForApplyId) {
        IsLikeResponse isLiked = universityService.getIsLiked(principal.getName(), universityInfoForApplyId);
        return ResponseEntity.ok(isLiked);
    }

    @PostMapping("/{universityInfoForApplyId}/like")
    public ResponseEntity<LikeResultResponse> addWishUniversity(
            Principal principal,
            @PathVariable Long universityInfoForApplyId) {
        LikeResultResponse likeResultResponse = universityService.likeUniversity(principal.getName(), universityInfoForApplyId);
        return ResponseEntity
                .ok(likeResultResponse);
    }

    @GetMapping("/detail/{universityInfoForApplyId}")
    public ResponseEntity<UniversityDetailResponse> getUniversityDetails(
            @PathVariable Long universityInfoForApplyId) {
        UniversityDetailResponse universityDetailResponse = universityService.getUniversityDetail(universityInfoForApplyId);
        return ResponseEntity.ok(universityDetailResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UniversityInfoForApplyPreviewResponse>> searchUniversity(
            @RequestParam(required = false, defaultValue = "") String region,
            @RequestParam(required = false, defaultValue = "") List<String> keyword,
            @RequestParam(required = false, defaultValue = "") LanguageTestType testType,
            @RequestParam(required = false, defaultValue = "") String testScore) {
        List<UniversityInfoForApplyPreviewResponse> universityInfoForApplyPreviewResponse
                = universityService.searchUniversity(region, keyword, testType, testScore);
        return ResponseEntity.ok(universityInfoForApplyPreviewResponse);
    }
}
