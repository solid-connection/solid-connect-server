package com.example.solidconnection.university.service;

import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

import static com.example.solidconnection.university.service.UniversityRecommendService.RECOMMEND_UNIVERSITY_NUM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("공통 추천 대학 서비스 테스트")
@TestContainerSpringBootTest
class GeneralRecommendUniversitiesTest extends BaseIntegrationTest {

    @Autowired
    private GeneralRecommendUniversities generalRecommendUniversities;

    @Value("${university.term}")
    private String term;

    @Test
    void 모집_시기의_대학들_중에서_랜덤하게_N개를_추천_목록으로_구성한다() {
        // given
        generalRecommendUniversities.init();
        List<UniversityInfoForApply> universities = generalRecommendUniversities.getRecommendUniversities();

        // when & then
        assertAll(
                () -> assertThat(universities)
                        .extracting("term")
                        .allMatch(term::equals),
                () -> assertThat(universities).hasSize(RECOMMEND_UNIVERSITY_NUM)
        );
    }
}
