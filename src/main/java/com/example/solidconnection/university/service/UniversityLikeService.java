package com.example.solidconnection.university.service;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.LikedUniversityRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
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

@RequiredArgsConstructor
@Service
public class UniversityLikeService {

    public static final String LIKE_SUCCESS_MESSAGE = "LIKE_SUCCESS";
    public static final String LIKE_CANCELED_MESSAGE = "LIKE_CANCELED";

    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;
    private final LikedUniversityRepository likedUniversityRepository;
    private final SiteUserRepository siteUserRepository;

    @Value("${university.term}")
    public String term;

    /*
     * 대학교를 '좋아요' 한다.
     * - 이미 좋아요가 눌러져있다면, 좋아요를 취소한다.
     * */
    @Transactional
    public LikeResultResponse likeUniversity(String email, Long universityInfoForApplyId) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        UniversityInfoForApply universityInfoForApply = universityInfoForApplyRepository.getUniversityInfoForApplyById(universityInfoForApplyId);

        Optional<LikedUniversity> alreadyLikedUniversity = likedUniversityRepository.findBySiteUserAndUniversityInfoForApply(siteUser, universityInfoForApply);
        if (alreadyLikedUniversity.isPresent()) {
            likedUniversityRepository.delete(alreadyLikedUniversity.get());
            return new LikeResultResponse(LIKE_CANCELED_MESSAGE);
        }

        LikedUniversity likedUniversity = LikedUniversity.builder()
                .universityInfoForApply(universityInfoForApply)
                .siteUser(siteUser)
                .build();
        likedUniversityRepository.save(likedUniversity);
        return new LikeResultResponse(LIKE_SUCCESS_MESSAGE);
    }

    /*
     * '좋아요'한 대학교인지 확인한다.
     * */
    @Transactional(readOnly = true)
    public IsLikeResponse getIsLiked(String email, Long universityInfoForApplyId) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        UniversityInfoForApply universityInfoForApply = universityInfoForApplyRepository.getUniversityInfoForApplyById(universityInfoForApplyId);
        boolean isLike = likedUniversityRepository.findBySiteUserAndUniversityInfoForApply(siteUser, universityInfoForApply).isPresent();
        return new IsLikeResponse(isLike);
    }
}
