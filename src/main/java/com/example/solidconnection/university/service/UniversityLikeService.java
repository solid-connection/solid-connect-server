package com.example.solidconnection.university.service;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.LikedUniversityRepository;
import com.example.solidconnection.university.domain.LikedUniversity;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.dto.IsLikeResponse;
import com.example.solidconnection.university.dto.LikeResultResponse;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.example.solidconnection.custom.exception.ErrorCode.ALREADY_LIKED_UNIVERSITY;
import static com.example.solidconnection.custom.exception.ErrorCode.NOT_LIKED_UNIVERSITY;

@RequiredArgsConstructor
@Service
public class UniversityLikeService {

    public static final String LIKE_SUCCESS_MESSAGE = "LIKE_SUCCESS";
    public static final String LIKE_CANCELED_MESSAGE = "LIKE_CANCELED";

    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;
    private final LikedUniversityRepository likedUniversityRepository;

    @Value("${university.term}")
    public String term;

    /*
     * 대학교를 '좋아요' 한다.
     * */
    @Transactional
    public LikeResultResponse likeUniversity(SiteUser siteUser, Long universityInfoForApplyId) {
        UniversityInfoForApply universityInfoForApply = universityInfoForApplyRepository.getUniversityInfoForApplyById(universityInfoForApplyId);

        Optional<LikedUniversity> optionalLikedUniversity = likedUniversityRepository.findBySiteUserAndUniversityInfoForApply(siteUser, universityInfoForApply);
        if (optionalLikedUniversity.isPresent()) {
            throw new CustomException(ALREADY_LIKED_UNIVERSITY);
        }

        LikedUniversity likedUniversity = LikedUniversity.builder()
                .universityInfoForApply(universityInfoForApply)
                .siteUser(siteUser)
                .build();
        likedUniversityRepository.save(likedUniversity);
        return new LikeResultResponse(LIKE_SUCCESS_MESSAGE);
    }

    /*
     * 대학교 '좋아요'를 취소한다.
     * */
    @Transactional
    public LikeResultResponse cancelLikeUniversity(SiteUser siteUser, long universityInfoForApplyId) throws CustomException {
        UniversityInfoForApply universityInfoForApply = universityInfoForApplyRepository.getUniversityInfoForApplyById(universityInfoForApplyId);

        Optional<LikedUniversity> optionalLikedUniversity = likedUniversityRepository.findBySiteUserAndUniversityInfoForApply(siteUser, universityInfoForApply);
        if (optionalLikedUniversity.isEmpty()) {
            throw new CustomException(NOT_LIKED_UNIVERSITY);
        }

        likedUniversityRepository.delete(optionalLikedUniversity.get());
        return new LikeResultResponse(LIKE_CANCELED_MESSAGE);
    }

    /*
     * '좋아요'한 대학교인지 확인한다.
     * */
    @Transactional(readOnly = true)
    public IsLikeResponse getIsLiked(SiteUser siteUser, Long universityInfoForApplyId) {
        UniversityInfoForApply universityInfoForApply = universityInfoForApplyRepository.getUniversityInfoForApplyById(universityInfoForApplyId);
        boolean isLike = likedUniversityRepository.findBySiteUserAndUniversityInfoForApply(siteUser, universityInfoForApply).isPresent();
        return new IsLikeResponse(isLike);
    }
}
