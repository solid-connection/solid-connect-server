package com.example.solidconnection.university.service;

import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.solidconnection.university.service.UnivApplyInfoRecommendService.RECOMMEND_UNIV_APPLY_INFO_NUM;

@Service
@RequiredArgsConstructor
public class GeneralUnivApplyInfoRecommendService {

    /*
     * 해당 시기에 열리는 대학교들 중 랜덤으로 선택해서 목록을 구성한다.
     * */
    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;

    @Getter
    private List<UnivApplyInfo> generalRecommends;

    @Value("${university.term}")
    public String term;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        generalRecommends = universityInfoForApplyRepository.findRandomByTerm(term, RECOMMEND_UNIV_APPLY_INFO_NUM);
    }
}
