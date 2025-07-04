package com.example.solidconnection.university.service;

import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.solidconnection.university.service.UnivApplyInfoRecommendService.RECOMMEND_UNIV_APPLY_INFO_NUM;

@Service
@RequiredArgsConstructor
public class GeneralUnivApplyInfoRecommendService {

    /*
     * 해당 시기에 열리는 대학교들 중 랜덤으로 선택해서 목록을 구성한다.
     * */
    private final UnivApplyInfoRepository univApplyInfoRepository;

    @Value("${university.term}")
    public String term;

    @Transactional(readOnly = true)
    public List<UnivApplyInfo> getGeneralRecommends() {
        return univApplyInfoRepository.findRandomByTerm(term, RECOMMEND_UNIV_APPLY_INFO_NUM);
    }
}
