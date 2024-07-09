package com.example.solidconnection.university.service;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.LikedUniversityRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.university.domain.LikedUniversity;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.dto.IsLikeResponse;
import com.example.solidconnection.university.dto.LikeResultResponse;
import com.example.solidconnection.university.dto.UniversityDetailResponse;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponse;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import com.example.solidconnection.university.repository.custom.UniversityFilterRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UniversityService {

    public static final String TERM = "2024-2-a";
    public static final String LIKE_SUCCESS_MESSAGE = "LIKE_SUCCESS";
    public static final String LIKE_CANCELED_MESSAGE = "LIKE_CANCELED";

    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;
    private final LikedUniversityRepository likedUniversityRepository;
    private final UniversityFilterRepositoryImpl universityFilterRepository;
    private final SiteUserRepository siteUserRepository;

    /*
     * 대학교 상세 정보를 불러온다.
     * - 대학교(University) 정보와 대학 지원 정보(UniversityInfoForApply) 정보를 조합하여 반환한다.
     * */
    @Transactional(readOnly = true)
    public UniversityDetailResponse getUniversityDetail(Long universityInfoForApplyId) {
        UniversityInfoForApply universityInfoForApply
                = universityInfoForApplyRepository.getUniversityInfoForApplyById(universityInfoForApplyId);
        University university = universityInfoForApply.getUniversity();

        return UniversityDetailResponse.of(university, universityInfoForApply);
    }

    /*
     * 대학교 검색 결과를 불러온다.
     * - 권역, 키워드, 언어 시험 종류, 언어 시험 점수를 조건으로 검색하여 결과를 반환한다.
     *   - 키워드는 국가명 또는 대학명에 포함되는 것이 조건이다.
     *   - 언어 시험 점수는 합격 최소 점수보다 높은 것이 조건이다.
     * */
    @Transactional(readOnly = true)
    public List<UniversityInfoForApplyPreviewResponse> searchUniversity(
            String regionCode, List<String> keywords, LanguageTestType testType, String testScore) {
        return universityFilterRepository
                .findByRegionCodeAndKeywordsAndLanguageTestTypeAndTestScore(regionCode, keywords, testType, testScore)
                .stream()
                .map(UniversityInfoForApplyPreviewResponse::from)
                .toList();
    }

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
