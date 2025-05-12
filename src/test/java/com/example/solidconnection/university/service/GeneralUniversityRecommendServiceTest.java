package com.example.solidconnection.university.service;

import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.fixture.UniversityInfoForApplyFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

import static com.example.solidconnection.university.service.UniversityRecommendService.RECOMMEND_UNIVERSITY_NUM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestContainerSpringBootTest
@DisplayName("공통 추천 대학 서비스 테스트")
class GeneralUniversityRecommendServiceTest {

    @Autowired
    private GeneralUniversityRecommendService generalUniversityRecommendService;

    @Autowired
    private UniversityInfoForApplyFixture universityInfoForApplyFixture;

    @Value("${university.term}")
    private String term;

    @BeforeEach
    void setUp() {
        universityInfoForApplyFixture.괌대학_A_지원_정보();
        universityInfoForApplyFixture.괌대학_B_지원_정보();
        universityInfoForApplyFixture.네바다주립대학_라스베이거스_지원_정보();
        universityInfoForApplyFixture.메모리얼대학_세인트존스_A_지원_정보();
        universityInfoForApplyFixture.서던덴마크대학교_지원_정보();
        universityInfoForApplyFixture.코펜하겐IT대학_지원_정보();
        universityInfoForApplyFixture.그라츠대학_지원_정보();
        universityInfoForApplyFixture.그라츠공과대학_지원_정보();
        universityInfoForApplyFixture.린츠_카톨릭대학_지원_정보();
        universityInfoForApplyFixture.메이지대학_지원_정보();
        generalUniversityRecommendService.init();
    }

    @Test
    void 모집_시기의_대학들_중에서_랜덤하게_N개를_추천_목록으로_구성한다() {
        // given
        List<UniversityInfoForApply> universities = generalUniversityRecommendService.getRecommendUniversities();

        // when & then
        assertAll(
                () -> assertThat(universities)
                        .extracting("term")
                        .allMatch(term::equals),
                () -> assertThat(universities).hasSize(RECOMMEND_UNIVERSITY_NUM)
        );
    }
}
