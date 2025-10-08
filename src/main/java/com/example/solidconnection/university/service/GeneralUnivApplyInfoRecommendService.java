package com.example.solidconnection.university.service;

import static com.example.solidconnection.common.exception.ErrorCode.CURRENT_TERM_NOT_FOUND;
import static com.example.solidconnection.university.service.UnivApplyInfoRecommendService.RECOMMEND_UNIV_APPLY_INFO_NUM;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.term.domain.Term;
import com.example.solidconnection.term.repository.TermRepository;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeneralUnivApplyInfoRecommendService {

    /*
     * 해당 시기에 열리는 대학교들 중 랜덤으로 선택해서 목록을 구성한다.
     * */
    private final UnivApplyInfoRepository univApplyInfoRepository;
    private final TermRepository termRepository;

    @Getter
    private List<UnivApplyInfo> generalRecommends;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        termRepository.findByIsCurrentTrue().ifPresent(term -> {
            Pageable page = PageRequest.of(0, RECOMMEND_UNIV_APPLY_INFO_NUM);
            generalRecommends = univApplyInfoRepository.findRandomByTermId(term.getId(), page);
        });
    }
}
