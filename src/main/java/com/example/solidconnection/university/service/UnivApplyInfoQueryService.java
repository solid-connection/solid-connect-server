package com.example.solidconnection.university.service;

import static com.example.solidconnection.common.exception.ErrorCode.CURRENT_TERM_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.TERM_NOT_FOUND;

import com.example.solidconnection.cache.annotation.ThunderingHerdCaching;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.term.domain.Term;
import com.example.solidconnection.term.repository.TermRepository;
import com.example.solidconnection.university.domain.HostUniversity;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.dto.UnivApplyInfoDetailResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponses;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UnivApplyInfoQueryService {

    private final UnivApplyInfoRepository univApplyInfoRepository;
    private final TermRepository termRepository;

    /*
     * 대학교 상세 정보를 불러온다.
     * - 대학교(University) 정보와 대학 지원 정보(UniversityInfoForApply) 정보를 조합하여 반환한다.
     * */
    @Transactional(readOnly = true)
    @ThunderingHerdCaching(key = "univApplyInfo:{0}", cacheManager = "customCacheManager", ttlSec = 86400)
    public UnivApplyInfoDetailResponse getUnivApplyInfoDetail(Long univApplyInfoId) {
        UnivApplyInfo univApplyInfo
                = univApplyInfoRepository.getUnivApplyInfoById(univApplyInfoId);
        HostUniversity university = univApplyInfo.getUniversity();

        Term term = termRepository.findById(univApplyInfo.getTermId())
                .orElseThrow(() -> new CustomException(TERM_NOT_FOUND));
        return UnivApplyInfoDetailResponse.of(university, univApplyInfo, term.getName());
    }

    @Transactional(readOnly = true)
    // todo: 현재 레디스 관련 에러 발생중으로 임시 주석처리, 추후 원인 분석 후 적용 필요
    // @ThunderingHerdCaching(key = "univApplyInfoTextSearch:{0}", cacheManager = "customCacheManager", ttlSec = 86400)
    public UnivApplyInfoPreviewResponses searchUnivApplyInfoByText(String text) {
        Term term = termRepository.findByIsCurrentTrue()
                .orElseThrow(() -> new CustomException(CURRENT_TERM_NOT_FOUND));

        List<UnivApplyInfo> univApplyInfos = univApplyInfoRepository.findAllByText(text, term.getId());

        List<UnivApplyInfoPreviewResponse> responses = univApplyInfos.stream()
                .map(univApplyInfo -> UnivApplyInfoPreviewResponse.of(univApplyInfo, term.getName()))
                .toList();
        return new UnivApplyInfoPreviewResponses(responses);
    }
}
