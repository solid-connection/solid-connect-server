package com.example.solidconnection.university.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.LikedUnivApplyInfo;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.dto.IsLikeResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponse;
import com.example.solidconnection.university.repository.LikedUnivApplyInfoRepository;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.solidconnection.common.exception.ErrorCode.ALREADY_LIKED_UNIV_APPLY_INFO;
import static com.example.solidconnection.common.exception.ErrorCode.NOT_LIKED_UNIV_APPLY_INFO;

@RequiredArgsConstructor
@Service
public class UnivApplyInfoLikeService {

    private final UnivApplyInfoRepository univApplyInfoRepository;
    private final LikedUnivApplyInfoRepository likedUnivApplyInfoRepository;

    @Value("${university.term}")
    public String term;

    /*
     * '좋아요'한 대학교 목록을 조회한다.
     * */
    @Transactional(readOnly = true)
    public List<UnivApplyInfoPreviewResponse> getLikedUnivApplyInfos(SiteUser siteUser) {
        List<UnivApplyInfo> univApplyInfos = likedUnivApplyInfoRepository.findUnivApplyInfosBySiteUserId(siteUser.getId());
        return univApplyInfos.stream()
                .map(UnivApplyInfoPreviewResponse::from)
                .toList();
    }

    /*
     * 대학교를 '좋아요' 한다.
     * */
    @Transactional
    public void addUnivApplyInfoLike(SiteUser siteUser, Long univApplyInfoId) {
        UnivApplyInfo univApplyInfo = univApplyInfoRepository.getUnivApplyInfoById(univApplyInfoId);

        Optional<LikedUnivApplyInfo> optionalLikedUnivApplyInfo = likedUnivApplyInfoRepository.findBySiteUserIdAndUnivApplyInfoId(siteUser.getId(), univApplyInfo.getId());
        if (optionalLikedUnivApplyInfo.isPresent()) {
            throw new CustomException(ALREADY_LIKED_UNIV_APPLY_INFO);
        }

        LikedUnivApplyInfo likedUnivApplyInfo = LikedUnivApplyInfo.builder()
                .univApplyInfoId(univApplyInfo.getId())
                .siteUserId(siteUser.getId())
                .build();
        likedUnivApplyInfoRepository.save(likedUnivApplyInfo);
    }

    /*
     * 대학교 '좋아요'를 취소한다.
     * */
    @Transactional
    public void cancelUnivApplyInfoLike(SiteUser siteUser, long univApplyInfoId) {
        UnivApplyInfo univApplyInfo = univApplyInfoRepository.getUnivApplyInfoById(univApplyInfoId);

        Optional<LikedUnivApplyInfo> optionalLikedUnivApplyInfo = likedUnivApplyInfoRepository.findBySiteUserIdAndUnivApplyInfoId(siteUser.getId(), univApplyInfo.getId());
        if (optionalLikedUnivApplyInfo.isEmpty()) {
            throw new CustomException(NOT_LIKED_UNIV_APPLY_INFO);
        }

        likedUnivApplyInfoRepository.delete(optionalLikedUnivApplyInfo.get());
    }

    /*
     * '좋아요'한 대학교인지 확인한다.
     * */
    @Transactional(readOnly = true)
    public IsLikeResponse isUnivApplyInfoLiked(SiteUser siteUser, Long univApplyInfoId) {
        UnivApplyInfo univApplyInfo = univApplyInfoRepository.getUnivApplyInfoById(univApplyInfoId);
        boolean isLike = likedUnivApplyInfoRepository.findBySiteUserIdAndUnivApplyInfoId(siteUser.getId(), univApplyInfo.getId()).isPresent();
        return new IsLikeResponse(isLike);
    }
}
