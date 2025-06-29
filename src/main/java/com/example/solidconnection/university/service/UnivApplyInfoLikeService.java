package com.example.solidconnection.university.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.repository.LikedUnivApplyInfoRepository;
import com.example.solidconnection.university.domain.LikedUnivApplyInfo;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.dto.IsLikeResponse;
import com.example.solidconnection.university.dto.LikeResultResponse;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.example.solidconnection.common.exception.ErrorCode.ALREADY_LIKED_UNIV_APPLY_INFO;
import static com.example.solidconnection.common.exception.ErrorCode.NOT_LIKED_UNIV_APPLY_INFO;

@RequiredArgsConstructor
@Service
public class UnivApplyInfoLikeService {

    public static final String LIKE_SUCCESS_MESSAGE = "LIKE_SUCCESS";
    public static final String LIKE_CANCELED_MESSAGE = "LIKE_CANCELED";

    private final UnivApplyInfoRepository univApplyInfoRepository;
    private final LikedUnivApplyInfoRepository likedUnivApplyInfoRepository;

    @Value("${university.term}")
    public String term;

    /*
     * 대학교를 '좋아요' 한다.
     * */
    @Transactional
    public LikeResultResponse likeUnivApplyInfo(SiteUser siteUser, Long univApplyInfoId) {
        UnivApplyInfo univApplyInfo = univApplyInfoRepository.getUnivApplyInfoById(univApplyInfoId);

        Optional<LikedUnivApplyInfo> optionalLikedUnivApplyInfo = likedUnivApplyInfoRepository.findBySiteUserAndUnivApplyInfo(siteUser, univApplyInfo);
        if (optionalLikedUnivApplyInfo.isPresent()) {
            throw new CustomException(ALREADY_LIKED_UNIV_APPLY_INFO);
        }

        LikedUnivApplyInfo likedUnivApplyInfo = LikedUnivApplyInfo.builder()
                .univApplyInfo(univApplyInfo)
                .siteUser(siteUser)
                .build();
        likedUnivApplyInfoRepository.save(likedUnivApplyInfo);
        return new LikeResultResponse(LIKE_SUCCESS_MESSAGE);
    }

    /*
     * 대학교 '좋아요'를 취소한다.
     * */
    @Transactional
    public LikeResultResponse cancelLikeUnivApplyInfo(SiteUser siteUser, long univApplyInfoId) {
        UnivApplyInfo univApplyInfo = univApplyInfoRepository.getUnivApplyInfoById(univApplyInfoId);

        Optional<LikedUnivApplyInfo> optionalLikedUnivApplyInfo = likedUnivApplyInfoRepository.findBySiteUserAndUnivApplyInfo(siteUser, univApplyInfo);
        if (optionalLikedUnivApplyInfo.isEmpty()) {
            throw new CustomException(NOT_LIKED_UNIV_APPLY_INFO);
        }

        likedUnivApplyInfoRepository.delete(optionalLikedUnivApplyInfo.get());
        return new LikeResultResponse(LIKE_CANCELED_MESSAGE);
    }

    /*
     * '좋아요'한 대학교인지 확인한다.
     * */
    @Transactional(readOnly = true)
    public IsLikeResponse getIsLiked(SiteUser siteUser, Long univApplyInfoId) {
        UnivApplyInfo univApplyInfo = univApplyInfoRepository.getUnivApplyInfoById(univApplyInfoId);
        boolean isLike = likedUnivApplyInfoRepository.findBySiteUserAndUnivApplyInfo(siteUser, univApplyInfo).isPresent();
        return new IsLikeResponse(isLike);
    }
}
