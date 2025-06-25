package com.example.solidconnection.university.controller;

import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.service.MyPageService;
import com.example.solidconnection.university.domain.LanguageTestType;
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
@RequestMapping("/univ-apply-infos")
@RestController
public class UnivApplyInfoController {

    private final UniversityQueryService universityQueryService;
    private final UniversityLikeService universityLikeService;
    private final UniversityRecommendService universityRecommendService;
    private final MyPageService myPageService;

    @GetMapping("/recommend")
    public ResponseEntity<UniversityRecommendsResponse> getUnivApplyInfoRecommends(
            @AuthorizedUser(required = false) SiteUser siteUser
    ) {
        if (siteUser == null) {
            return ResponseEntity.ok(universityRecommendService.getGeneralRecommends());
        } else {
            return ResponseEntity.ok(universityRecommendService.getPersonalRecommends(siteUser));
        }
    }

    @GetMapping("/like")
    public ResponseEntity<List<UniversityInfoForApplyPreviewResponse>> getMyWishUnivApplyInfo( /* todo: wish 가 아니라 liked 로 변경 필요 - 코드 용어 통일 */
            @AuthorizedUser SiteUser siteUser
    ) {
        List<UniversityInfoForApplyPreviewResponse> wishUniversities = myPageService.getWishUniversity(siteUser);
        return ResponseEntity.ok(wishUniversities);
    }

    @GetMapping("/{univ-apply-info-id}/like")
    public ResponseEntity<IsLikeResponse> getIsLiked(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("univ-apply-info-id") Long univApplyInfoId
    ) {
        IsLikeResponse isLiked = universityLikeService.getIsLiked(siteUser, univApplyInfoId);
        return ResponseEntity.ok(isLiked);
    }

    @PostMapping("/{univ-apply-info-id}/like")
    public ResponseEntity<LikeResultResponse> addWishUnivApplyInfo(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("univ-apply-info-id") Long univApplyInfoId
    ) {
        LikeResultResponse likeResultResponse = universityLikeService.likeUniversity(siteUser, univApplyInfoId);
        return ResponseEntity.ok(likeResultResponse);
    }

    @DeleteMapping("/{univ-apply-info-id}/like")
    public ResponseEntity<LikeResultResponse> cancelWishUnivApplyInfo(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("univ-apply-info-id") Long univApplyInfoId
    ) {
        LikeResultResponse likeResultResponse = universityLikeService.cancelLikeUniversity(siteUser, univApplyInfoId);
        return ResponseEntity.ok(likeResultResponse);
    }

    @GetMapping("/{univ-apply-info-id}")
    public ResponseEntity<UniversityDetailResponse> getUnivApplyInfoDetails(
            @PathVariable("univ-apply-info-id") Long univApplyInfoId
    ) {
        UniversityDetailResponse universityDetailResponse = universityQueryService.getUniversityDetail(univApplyInfoId);
        return ResponseEntity.ok(universityDetailResponse);
    }

    // todo return타입 UniversityInfoForApplyPreviewResponses로 추후 수정 필요
    @GetMapping("/search")
    public ResponseEntity<List<UniversityInfoForApplyPreviewResponse>> searchUnivApplyInfo(
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
