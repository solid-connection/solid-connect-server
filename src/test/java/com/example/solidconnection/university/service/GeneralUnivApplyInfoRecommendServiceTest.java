package com.example.solidconnection.university.service;

import static com.example.solidconnection.university.service.UnivApplyInfoRecommendService.RECOMMEND_UNIV_APPLY_INFO_NUM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.term.domain.Term;
import com.example.solidconnection.term.fixture.TermFixture;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.fixture.UnivApplyInfoFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("대학 지원 정보 공통 추천 서비스 테스트")
class GeneralUnivApplyInfoRecommendServiceTest {

    @Autowired
    private GeneralUnivApplyInfoRecommendService generalUnivApplyInfoRecommendService;

    @Autowired
    private UnivApplyInfoFixture univApplyInfoFixture;

    @Autowired
    private TermFixture termFixture;

    private Term term;

    @BeforeEach
    void setUp() {
        term = termFixture.현재_학기("2025-2");

        univApplyInfoFixture.괌대학_A_지원_정보(term.getId());
        univApplyInfoFixture.괌대학_B_지원_정보(term.getId());
        univApplyInfoFixture.네바다주립대학_라스베이거스_지원_정보(term.getId());
        univApplyInfoFixture.메모리얼대학_세인트존스_A_지원_정보(term.getId());
        univApplyInfoFixture.서던덴마크대학교_지원_정보(term.getId());
        univApplyInfoFixture.코펜하겐IT대학_지원_정보(term.getId());
        univApplyInfoFixture.그라츠대학_지원_정보(term.getId());
        univApplyInfoFixture.그라츠공과대학_지원_정보(term.getId());
        univApplyInfoFixture.린츠_카톨릭대학_지원_정보(term.getId());
        univApplyInfoFixture.메이지대학_지원_정보(term.getId());
        generalUnivApplyInfoRecommendService.init();
    }

    @Test
    void 모집_시기의_대학_지원_정보_중에서_랜덤하게_N개를_추천_목록으로_구성한다() {
        // given
        List<UnivApplyInfo> universities = generalUnivApplyInfoRecommendService.getGeneralRecommends();

        // when & then
        assertAll(
                () -> assertThat(universities)
                        .extracting("termId")
                        .allMatch(term.getId()::equals),
                () -> assertThat(universities).hasSize(RECOMMEND_UNIV_APPLY_INFO_NUM)
        );
    }
}
