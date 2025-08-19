package com.example.solidconnection.university.service;

import com.example.solidconnection.cache.annotation.ThunderingHerdCaching;
import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.dto.UnivApplyInfoDetailResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponses;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UnivApplyInfoQueryService {

    private final UnivApplyInfoRepository univApplyInfoRepository;

    @Value("${university.term}")
    public String term;

    /*
     * 대학교 상세 정보를 불러온다.
     * - 대학교(University) 정보와 대학 지원 정보(UniversityInfoForApply) 정보를 조합하여 반환한다.
     * */
    @Transactional(readOnly = true)
    @ThunderingHerdCaching(key = "univApplyInfo:{0}", cacheManager = "customCacheManager", ttlSec = 86400)
    public UnivApplyInfoDetailResponse getUnivApplyInfoDetail(Long univApplyInfoId) {
        UnivApplyInfo univApplyInfo
                = univApplyInfoRepository.getUnivApplyInfoById(univApplyInfoId);
        University university = univApplyInfo.getUniversity();

        return UnivApplyInfoDetailResponse.of(university, univApplyInfo);
    }

    /*
     * 대학교 검색 결과를 불러온다.
     * - 권역, 키워드, 언어 시험 종류, 언어 시험 점수를 조건으로 검색하여 결과를 반환한다.
     *   - 권역은 영어 대문자로 받는다 e.g. ASIA
     *   - 키워드는 국가명 또는 대학명에 포함되는 것이 조건이다.
     *   - 언어 시험 점수는 합격 최소 점수보다 높은 것이 조건이다.
     * */
    @Transactional(readOnly = true)
    @ThunderingHerdCaching(key = "univApplyInfo:{0}:{1}:{2}:{3}", cacheManager = "customCacheManager", ttlSec = 86400)
    public UnivApplyInfoPreviewResponses searchUnivApplyInfo(
            String regionCode, List<String> keywords, LanguageTestType testType, String testScore
    ) {
        List<UnivApplyInfoPreviewResponse> res = univApplyInfoRepository
                .findAllByRegionCodeAndKeywordsAndLanguageTestTypeAndTestScoreAndTerm(regionCode, keywords, testType, testScore, term)
                .stream()
                .map(UnivApplyInfoPreviewResponse::from)
                .toList();
        return new UnivApplyInfoPreviewResponses(res);
    }
}
