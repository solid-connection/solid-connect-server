package com.example.solidconnection.university.controller;

import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.university.dto.IsLikeResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoDetailResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoFilterSearchRequest;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponses;
import com.example.solidconnection.university.dto.UnivApplyInfoRecommendsResponse;
import com.example.solidconnection.university.service.LikedUnivApplyInfoService;
import com.example.solidconnection.university.service.UnivApplyInfoQueryService;
import com.example.solidconnection.university.service.UnivApplyInfoRecommendService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/univ-apply-infos")
@RestController
public class UnivApplyInfoController {

    private final UnivApplyInfoQueryService univApplyInfoQueryService;
    private final LikedUnivApplyInfoService likedUnivApplyInfoService;
    private final UnivApplyInfoRecommendService univApplyInfoRecommendService;

    @Value("${university.term}")
    public String term;

    @GetMapping("/recommend")
    public ResponseEntity<UnivApplyInfoRecommendsResponse> getUnivApplyInfoRecommends(
            @AuthorizedUser(required = false) Long siteUserId
    ) {
        if (siteUserId == null) {
            return ResponseEntity.ok(univApplyInfoRecommendService.getGeneralRecommends());
        } else {
            return ResponseEntity.ok(univApplyInfoRecommendService.getPersonalRecommends(siteUserId));
        }
    }

    // todo: return 타입 UnivApplyInfoPreviewResponses 같이 객체로 묶어서 반환하는 것으로 변경 필요
    @GetMapping("/like")
    public ResponseEntity<List<UnivApplyInfoPreviewResponse>> getLikedUnivApplyInfos(
            @AuthorizedUser long siteUserId
    ) {
        List<UnivApplyInfoPreviewResponse> likedUnivApplyInfos = likedUnivApplyInfoService.getLikedUnivApplyInfos(siteUserId);
        return ResponseEntity.ok(likedUnivApplyInfos);
    }

    @GetMapping("/{univ-apply-info-id}/like")
    public ResponseEntity<IsLikeResponse> isUnivApplyInfoLiked(
            @AuthorizedUser long siteUserId,
            @PathVariable("univ-apply-info-id") Long univApplyInfoId
    ) {
        IsLikeResponse isLiked = likedUnivApplyInfoService.isUnivApplyInfoLiked(siteUserId, univApplyInfoId);
        return ResponseEntity.ok(isLiked);
    }

    @PostMapping("/{univ-apply-info-id}/like")
    public ResponseEntity<Void> addUnivApplyInfoLike(
            @AuthorizedUser long siteUserId,
            @PathVariable("univ-apply-info-id") Long univApplyInfoId
    ) {
        likedUnivApplyInfoService.addUnivApplyInfoLike(siteUserId, univApplyInfoId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{univ-apply-info-id}/like")
    public ResponseEntity<Void> cancelUnivApplyInfoLike(
            @AuthorizedUser long siteUserId,
            @PathVariable("univ-apply-info-id") Long univApplyInfoId
    ) {
        likedUnivApplyInfoService.cancelUnivApplyInfoLike(siteUserId, univApplyInfoId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{univ-apply-info-id}")
    public ResponseEntity<UnivApplyInfoDetailResponse> getUnivApplyInfoDetails(
            @PathVariable("univ-apply-info-id") Long univApplyInfoId
    ) {
        UnivApplyInfoDetailResponse univApplyInfoDetailResponse = univApplyInfoQueryService.getUnivApplyInfoDetail(univApplyInfoId);
        return ResponseEntity.ok(univApplyInfoDetailResponse);
    }

    @GetMapping("/search/filter")
    public ResponseEntity<UnivApplyInfoPreviewResponses> searchUnivApplyInfoByFilter(
            @Valid @ModelAttribute UnivApplyInfoFilterSearchRequest request
    ) {
        UnivApplyInfoPreviewResponses response = univApplyInfoQueryService.searchUnivApplyInfoByFilter(request, term);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/text")
    public ResponseEntity<UnivApplyInfoPreviewResponses> searchUnivApplyInfoByText(
            @RequestParam(required = false) String value
    ) {
        UnivApplyInfoPreviewResponses response = univApplyInfoQueryService.searchUnivApplyInfoByText(value, term);
        return ResponseEntity.ok(response);
    }
}
