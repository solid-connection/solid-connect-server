package com.example.solidconnection.university.service;

import com.example.solidconnection.repositories.CountryRepository;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.example.solidconnection.university.service.UniversityRecommendService.RECOMMEND_UNIVERSITY_NUM;

@RequiredArgsConstructor
@Component
public class GeneralRecommendUniversities {

    /*
     * 매 선발 시기(term) 마다 지원할 수 있는 대학교가 달라지므르, 추천 대학교도 달라져야 한다.
     * 하지만 매번 추천 대학교를 바꾸기에는 번거롭다.
     * 따라서 '추천 대학교 후보'들을 설정하고, DB 에서 현재 term 에 대해 찾아지는 대학교만 추천 대학교로 지정한다.
     * */
    @Getter
    private final List<UniversityInfoForApply> recommendUniversities;
    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;
    private final CountryRepository countryRepository;
    private final List<String> candidates = List.of(
            "오스트라바 대학", "RMIT멜버른공과대학(A형)", "알브슈타트 지그마링엔 대학",
            "뉴저지시티대학(A형)", "도요대학", "템플대학(A형)", "빈 공과대학교",
            "리스본대학 공과대학", "바덴뷔르템베르크 산학협력대학", "긴다이대학", "네바다주립대학 라스베이거스(B형)", "릴 가톨릭 대학",
            "그라츠공과대학", "그라츠 대학", "코펜하겐 IT대학", "메이지대학", "분쿄가쿠인대학", "린츠 카톨릭 대학교"
    );

    @Value("${university.term}")
    public String term;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        int i = 0;
        while (recommendUniversities.size() < RECOMMEND_UNIVERSITY_NUM && i < candidates.size()) {
            universityInfoForApplyRepository.findByKoreanNameAndTerm(candidates.get(i), term)
                    .ifPresent(recommendUniversities::add);
            i++;
        }
    }
}
