package com.example.solidconnection.university.controller;

import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.service.MyPageService;
import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.university.dto.IsLikeResponse;
import com.example.solidconnection.university.dto.LikeResultResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoDetailResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoRecommendsResponse;
import com.example.solidconnection.university.service.UnivApplyInfoLikeService;
import com.example.solidconnection.university.service.UnivApplyInfoQueryService;
import com.example.solidconnection.university.service.UnivApplyInfoRecommendService;
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

    private final UnivApplyInfoQueryService univApplyInfoQueryService;
    private final UnivApplyInfoLikeService univApplyInfoLikeService;
    private final UnivApplyInfoRecommendService univApplyInfoRecommendService;
    private final MyPageService myPageService;

    @GetMapping("/recommend")
    public ResponseEntity<UnivApplyInfoRecommendsResponse> getUnivApplyInfoRecommends(
            @AuthorizedUser(required = false) SiteUser siteUser
    ) {
        if (siteUser == null) {
            return ResponseEntity.ok(univApplyInfoRecommendService.getGeneralRecommends());
        } else {
            return ResponseEntity.ok(univApplyInfoRecommendService.getPersonalRecommends(siteUser));
        }
    }

    @GetMapping("/like")
    public ResponseEntity<List<UnivApplyInfoPreviewResponse>> getMyWishUnivApplyInfo( /* todo: wish 가 아니라 liked 로 변경 필요 - 코드 용어 통일 */
            @AuthorizedUser SiteUser siteUser
    ) {
        List<UnivApplyInfoPreviewResponse> wishUniversities = myPageService.getWishUnivApplyInfo(siteUser);
        return ResponseEntity.ok(wishUniversities);
    }

    @GetMapping("/{univ-apply-info-id}/like")
    public ResponseEntity<IsLikeResponse> getIsLiked(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("univ-apply-info-id") Long univApplyInfoId
    ) {
        IsLikeResponse isLiked = univApplyInfoLikeService.getIsLiked(siteUser, univApplyInfoId);
        return ResponseEntity.ok(isLiked);
    }

    @PostMapping("/{univ-apply-info-id}/like")
    public ResponseEntity<LikeResultResponse> addWishUnivApplyInfo(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("univ-apply-info-id") Long univApplyInfoId
    ) {
        LikeResultResponse likeResultResponse = univApplyInfoLikeService.likeUnivApplyInfo(siteUser, univApplyInfoId);
        return ResponseEntity.ok(likeResultResponse);
    }

    @DeleteMapping("/{univ-apply-info-id}/like")
    public ResponseEntity<LikeResultResponse> cancelWishUnivApplyInfo(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("univ-apply-info-id") Long univApplyInfoId
    ) {
        LikeResultResponse likeResultResponse = univApplyInfoLikeService.cancelLikeUnivApplyInfo(siteUser, univApplyInfoId);
        return ResponseEntity.ok(likeResultResponse);
    }

    @GetMapping("/{univ-apply-info-id}")
    public ResponseEntity<UnivApplyInfoDetailResponse> getUnivApplyInfoDetails(
            @PathVariable("univ-apply-info-id") Long univApplyInfoId
    ) {
        UnivApplyInfoDetailResponse univApplyInfoDetailResponse = univApplyInfoQueryService.getUnivApplyInfoDetail(univApplyInfoId);
        return ResponseEntity.ok(univApplyInfoDetailResponse);
    }

    // todo: return타입 UniversityInfoForApplyPreviewResponses로 추후 수정 필요
    @GetMapping("/search")
    public ResponseEntity<List<UnivApplyInfoPreviewResponse>> searchUnivApplyInfo(
            @RequestParam(required = false, defaultValue = "") String region,
            @RequestParam(required = false, defaultValue = "") List<String> keyword,
            @RequestParam(required = false, defaultValue = "") LanguageTestType testType,
            @RequestParam(required = false, defaultValue = "") String testScore
    ) {
        List<UnivApplyInfoPreviewResponse> univApplyInfoPreviewResponse
                = univApplyInfoQueryService.searchUnivApplyInfo(region, keyword, testType, testScore).univApplyInfoPreviews();
        return ResponseEntity.ok(univApplyInfoPreviewResponse);
    }
}
