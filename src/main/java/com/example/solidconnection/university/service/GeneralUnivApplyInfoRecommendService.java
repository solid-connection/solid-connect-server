package com.example.solidconnection.university.service;

import static com.example.solidconnection.university.service.UnivApplyInfoRecommendService.RECOMMEND_UNIV_APPLY_INFO_NUM;

import com.example.solidconnection.cache.annotation.ThunderingHerdCaching;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoRecommendsResponse;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GeneralUnivApplyInfoRecommendService {

    private static final long GENERAL_RECOMMEND_CACHE_TTL_SEC = 3600;

    /*
     * 해당 시기에 열리는 대학교들 중 랜덤으로 선택해서 목록을 구성한다.
     * */
    private final UnivApplyInfoRepository univApplyInfoRepository;

    @Transactional(readOnly = true)
    @ThunderingHerdCaching(
            key = "university:recommend:general:{0}",
            cacheManager = "customCacheManager",
            ttlSec = GENERAL_RECOMMEND_CACHE_TTL_SEC
    )
    public UnivApplyInfoRecommendsResponse getGeneralRecommends(long termId, String termName) {
        Pageable page = PageRequest.of(0, RECOMMEND_UNIV_APPLY_INFO_NUM);
        List<UnivApplyInfo> generalRecommends = univApplyInfoRepository.findRandomByTermId(termId, page);

        return new UnivApplyInfoRecommendsResponse(generalRecommends.stream()
                                                           .map(univApplyInfo -> UnivApplyInfoPreviewResponse.of(univApplyInfo, termName))
                                                           .toList());
    }
}
