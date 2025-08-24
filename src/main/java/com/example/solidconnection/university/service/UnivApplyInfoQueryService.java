package com.example.solidconnection.university.service;

import com.example.solidconnection.cache.annotation.ThunderingHerdCaching;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.dto.UnivApplyInfoDetailResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoFilterSearchRequest;
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

    @Transactional(readOnly = true)
    public UnivApplyInfoPreviewResponses searchUnivApplyInfoByFilter(UnivApplyInfoFilterSearchRequest request) {
        List<UnivApplyInfoPreviewResponse> responses = univApplyInfoRepository
                .findAllByFilter(request.languageTestType(), request.testScore(), term, request.countryCode())
                .stream()
                .map(UnivApplyInfoPreviewResponse::from)
                .toList();
        return new UnivApplyInfoPreviewResponses(responses);
    }

    @Transactional(readOnly = true)
    public UnivApplyInfoPreviewResponses searchUnivApplyInfoByText(String text) {
        // todo: 구현
        return null;
    }
}
