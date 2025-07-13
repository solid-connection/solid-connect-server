package com.example.solidconnection.university.service;

import static com.example.solidconnection.university.service.UnivApplyInfoRecommendService.RECOMMEND_UNIV_APPLY_INFO_NUM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.fixture.UnivApplyInfoFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@TestContainerSpringBootTest
@DisplayName("대학 지원 정보 공통 추천 서비스 테스트")
class GeneralUnivApplyInfoRecommendServiceTest {

    @Autowired
    private GeneralUnivApplyInfoRecommendService generalUnivApplyInfoRecommendService;

    @Autowired
    private UnivApplyInfoFixture univApplyInfoFixture;

    @Value("${university.term}")
    private String term;

    @BeforeEach
    void setUp() {
        univApplyInfoFixture.괌대학_A_지원_정보();
        univApplyInfoFixture.괌대학_B_지원_정보();
        univApplyInfoFixture.네바다주립대학_라스베이거스_지원_정보();
        univApplyInfoFixture.메모리얼대학_세인트존스_A_지원_정보();
        univApplyInfoFixture.서던덴마크대학교_지원_정보();
        univApplyInfoFixture.코펜하겐IT대학_지원_정보();
        univApplyInfoFixture.그라츠대학_지원_정보();
        univApplyInfoFixture.그라츠공과대학_지원_정보();
        univApplyInfoFixture.린츠_카톨릭대학_지원_정보();
        univApplyInfoFixture.메이지대학_지원_정보();
        generalUnivApplyInfoRecommendService.init();
    }

    @Test
    void 모집_시기의_대학_지원_정보_중에서_랜덤하게_N개를_추천_목록으로_구성한다() {
        // given
        List<UnivApplyInfo> universities = generalUnivApplyInfoRecommendService.getGeneralRecommends();

        // when & then
        assertAll(
                () -> assertThat(universities)
                        .extracting("term")
                        .allMatch(term::equals),
                () -> assertThat(universities).hasSize(RECOMMEND_UNIV_APPLY_INFO_NUM)
        );
    }
}
