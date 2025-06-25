package com.example.solidconnection.university.service;

import com.example.solidconnection.cache.annotation.ThunderingHerdCaching;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponse;
import com.example.solidconnection.university.dto.UniversityRecommendsResponse;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UnivApplyInfoRecommendService {

    public static final int RECOMMEND_UNIV_APPLY_INFO_NUM = 6;

    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;
    private final GeneralUnivApplyInfoRecommendService generalUnivApplyInfoRecommendService;

    @Value("${university.term}")
    private String term;

    /*
     * 사용자 맞춤 추천 대학교를 불러온다.
     * - 회원가입 시 선택한 관심 지역과 관심 국가에 해당하는 대학 중, 이번 term 에 열리는 학교들을 불러온다.
     * - 불러온 맞춤 추천 대학교의 순서를 무작위로 섞는다.
     * - 맞춤 추천 대학교의 수가 6개보다 적다면, 공통 추천 대학교 후보에서 이번 term 에 열리는 학교들을 부족한 수 만큼 불러온다.
     * */
    @Transactional(readOnly = true)
    public UniversityRecommendsResponse getPersonalRecommends(SiteUser siteUser) {
        // 맞춤 추천 대학교를 불러온다.
        List<UnivApplyInfo> personalRecommends = universityInfoForApplyRepository
                .findUniversityInfoForAppliesBySiteUsersInterestedCountryOrRegionAndTerm(siteUser, term);
        List<UnivApplyInfo> trimmedRecommends
                = personalRecommends.subList(0, Math.min(RECOMMEND_UNIV_APPLY_INFO_NUM, personalRecommends.size()));
        Collections.shuffle(trimmedRecommendUniversities);

        // 맞춤 추천 대학교의 수가 6개보다 적다면, 일반 추천 대학교를 부족한 수 만큼 불러온다.
        if (trimmedRecommends.size() < RECOMMEND_UNIV_APPLY_INFO_NUM) {
            trimmedRecommends.addAll(getGeneralRecommendsExcludingSelected(trimmedRecommends));
        }

        return new UniversityRecommendsResponse(trimmedRecommends.stream()
                .map(UniversityInfoForApplyPreviewResponse::from)
                .toList());
    }

    private List<UnivApplyInfo> getGeneralRecommendsExcludingSelected(List<UnivApplyInfo> alreadyPicked) {
        List<UnivApplyInfo> generalRecommend = new ArrayList<>(generalUnivApplyInfoRecommendService.getGeneralRecommends());
        generalRecommend.removeAll(alreadyPicked);
        Collections.shuffle(generalRecommend);
        return generalRecommend.subList(0, RECOMMEND_UNIV_APPLY_INFO_NUM - alreadyPicked.size());
    }

    /*
     * 공통 추천 대학교를 불러온다.
     * */
    @Transactional(readOnly = true)
    @ThunderingHerdCaching(key = "university:recommend:general", cacheManager = "customCacheManager", ttlSec = 86400)
    public UniversityRecommendsResponse getGeneralRecommends() {
        List<UnivApplyInfo> generalRecommends = new ArrayList<>(generalUnivApplyInfoRecommendService.getGeneralRecommends());
        return new UniversityRecommendsResponse(generalRecommends.stream()
                .map(UniversityInfoForApplyPreviewResponse::from)
                .toList());
    }
}
