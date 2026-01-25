package com.example.solidconnection.university.service;

import static com.example.solidconnection.common.exception.ErrorCode.ALREADY_LIKED_UNIV_APPLY_INFO;
import static com.example.solidconnection.common.exception.ErrorCode.NOT_LIKED_UNIV_APPLY_INFO;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.term.domain.Term;
import com.example.solidconnection.term.repository.TermRepository;
import com.example.solidconnection.university.domain.HomeUniversity;
import com.example.solidconnection.university.domain.LikedUnivApplyInfo;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.dto.IsLikeResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponse;
import com.example.solidconnection.university.repository.HomeUniversityRepository;
import com.example.solidconnection.university.repository.LikedUnivApplyInfoRepository;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LikedUnivApplyInfoService {

    private final UnivApplyInfoRepository univApplyInfoRepository;
    private final HomeUniversityRepository homeUniversityRepository;
    private final LikedUnivApplyInfoRepository likedUnivApplyInfoRepository;
    private final TermRepository termRepository;

    /*
     * '좋아요'한 대학교 목록을 조회한다.
     * */
    @Transactional(readOnly = true)
    public List<UnivApplyInfoPreviewResponse> getLikedUnivApplyInfos(long siteUserId) {
        List<UnivApplyInfo> univApplyInfos = likedUnivApplyInfoRepository.findUnivApplyInfosBySiteUserId(siteUserId);
        Set<Long> termIds = univApplyInfos.stream()
                .map(UnivApplyInfo::getTermId)
                .collect(Collectors.toSet());

        Map<Long, String> termMap = termRepository.findAllById(termIds).stream()
                .collect(Collectors.toMap(Term::getId, Term::getName));
        Map<Long, HomeUniversity> homeUniversityMap = getHomeUniversityMap(univApplyInfos);

        return univApplyInfos.stream()
                .map(univApplyInfo -> {
                    String termName = termMap.getOrDefault(univApplyInfo.getTermId(), "Unknown");
                    HomeUniversity homeUniversity = homeUniversityMap.get(univApplyInfo.getHomeUniversityId());
                    String homeUniversityName = homeUniversity != null ? homeUniversity.getName() : null;
                    return UnivApplyInfoPreviewResponse.of(univApplyInfo, termName, homeUniversityName);
                })
                .toList();
    }

    private Map<Long, HomeUniversity> getHomeUniversityMap(List<UnivApplyInfo> univApplyInfos) {
        List<Long> homeUniversityIds = univApplyInfos.stream()
                .map(UnivApplyInfo::getHomeUniversityId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return homeUniversityRepository.findAllByIdIn(homeUniversityIds).stream()
                .collect(Collectors.toMap(HomeUniversity::getId, Function.identity()));
    }

    /*
     * 대학교를 '좋아요' 한다.
     * */
    @Transactional
    public void addUnivApplyInfoLike(long siteUserId, Long univApplyInfoId) {
        UnivApplyInfo univApplyInfo = univApplyInfoRepository.getUnivApplyInfoById(univApplyInfoId);

        Optional<LikedUnivApplyInfo> optionalLikedUnivApplyInfo = likedUnivApplyInfoRepository.findBySiteUserIdAndUnivApplyInfoId(siteUserId, univApplyInfo.getId());
        if (optionalLikedUnivApplyInfo.isPresent()) {
            throw new CustomException(ALREADY_LIKED_UNIV_APPLY_INFO);
        }

        LikedUnivApplyInfo likedUnivApplyInfo = LikedUnivApplyInfo.builder()
                .univApplyInfoId(univApplyInfo.getId())
                .siteUserId(siteUserId)
                .build();
        likedUnivApplyInfoRepository.save(likedUnivApplyInfo);
    }

    /*
     * 대학교 '좋아요'를 취소한다.
     * */
    @Transactional
    public void cancelUnivApplyInfoLike(long siteUserId, long univApplyInfoId) {
        UnivApplyInfo univApplyInfo = univApplyInfoRepository.getUnivApplyInfoById(univApplyInfoId);

        Optional<LikedUnivApplyInfo> optionalLikedUnivApplyInfo = likedUnivApplyInfoRepository.findBySiteUserIdAndUnivApplyInfoId(siteUserId, univApplyInfo.getId());
        if (optionalLikedUnivApplyInfo.isEmpty()) {
            throw new CustomException(NOT_LIKED_UNIV_APPLY_INFO);
        }

        likedUnivApplyInfoRepository.delete(optionalLikedUnivApplyInfo.get());
    }

    /*
     * '좋아요'한 대학교인지 확인한다.
     * */
    @Transactional(readOnly = true)
    public IsLikeResponse isUnivApplyInfoLiked(long siteUserId, Long univApplyInfoId) {
        UnivApplyInfo univApplyInfo = univApplyInfoRepository.getUnivApplyInfoById(univApplyInfoId);
        boolean isLike = likedUnivApplyInfoRepository.findBySiteUserIdAndUnivApplyInfoId(siteUserId, univApplyInfo.getId()).isPresent();
        return new IsLikeResponse(isLike);
    }
}
