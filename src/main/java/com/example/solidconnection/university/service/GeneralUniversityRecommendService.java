package com.example.solidconnection.university.service;

import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.solidconnection.university.service.UniversityRecommendService.RECOMMEND_UNIVERSITY_NUM;

@Service
@RequiredArgsConstructor
public class GeneralUniversityRecommendService {

    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;

    @Value("${university.term}")
    public String term;

    @Transactional(readOnly = true)  // 트랜잭션 추가
    public List<UnivApplyInfo> getRecommendUniversities() {
        List<UnivApplyInfo> universities = universityInfoForApplyRepository.findRandomByTerm(term, RECOMMEND_UNIVERSITY_NUM);

        universities.forEach(univ -> {
            Hibernate.initialize(univ.getLanguageRequirements());
            Hibernate.initialize(univ.getUniversity());
            if (univ.getUniversity() != null) {
                Hibernate.initialize(univ.getUniversity().getCountry());
                Hibernate.initialize(univ.getUniversity().getRegion());
            }
        });

        return universities;
    }
}
