package com.example.solidconnection.university.service;

import static com.example.solidconnection.university.service.UnivApplyInfoRecommendService.RECOMMEND_UNIV_APPLY_INFO_NUM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.cache.manager.CustomCacheManager;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.term.domain.Term;
import com.example.solidconnection.term.fixture.TermFixture;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoRecommendsResponse;
import com.example.solidconnection.university.fixture.UnivApplyInfoFixture;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
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
    private UnivApplyInfoRepository univApplyInfoRepository;

    @Autowired
    private CustomCacheManager cacheManager;

    @Autowired
    private TermFixture termFixture;

    private Term term;
    private List<UnivApplyInfo> currentTermUnivApplyInfos;

    @BeforeEach
    void setUp() {
        term = termFixture.현재_학기("2025-2");

        currentTermUnivApplyInfos = List.of(
                univApplyInfoFixture.괌대학_A_지원_정보(term.getId()),
                univApplyInfoFixture.버지니아공과대학_지원_정보(term.getId()),
                univApplyInfoFixture.네바다주립대학_라스베이거스_지원_정보(term.getId()),
                univApplyInfoFixture.메모리얼대학_세인트존스_A_지원_정보(term.getId()),
                univApplyInfoFixture.서던덴마크대학교_지원_정보(term.getId()),
                univApplyInfoFixture.코펜하겐IT대학_지원_정보(term.getId())
        );
    }

    @Test
    void 모집_시기의_대학_지원_정보_중에서_랜덤하게_N개를_추천_목록으로_구성한다() {
        // when
        UnivApplyInfoRecommendsResponse response = generalUnivApplyInfoRecommendService.getGeneralRecommends(
                term.getId(),
                term.getName()
        );
        List<Long> currentTermUnivApplyInfoIds = currentTermUnivApplyInfos.stream()
                .map(UnivApplyInfo::getId)
                .toList();

        // then
        assertAll(
                () -> assertThat(response.recommendedUniversities()).hasSize(RECOMMEND_UNIV_APPLY_INFO_NUM),
                () -> assertThat(response.recommendedUniversities())
                        .extracting(UnivApplyInfoPreviewResponse::id)
                        .isSubsetOf(currentTermUnivApplyInfoIds)
        );
    }

    @Test
    void 캐시를_삭제하면_서버_재기동_없이_DB_기준으로_추천_목록을_다시_구성한다() {
        // given
        UnivApplyInfo deletedUnivApplyInfo = currentTermUnivApplyInfos.get(0);
        UnivApplyInfoRecommendsResponse cachedResponse = generalUnivApplyInfoRecommendService.getGeneralRecommends(
                term.getId(),
                term.getName()
        );

        univApplyInfoRepository.delete(deletedUnivApplyInfo);
        UnivApplyInfo addedUnivApplyInfo = univApplyInfoFixture.그라츠대학_지원_정보(term.getId());

        // when
        UnivApplyInfoRecommendsResponse responseBeforeCacheEvict = generalUnivApplyInfoRecommendService.getGeneralRecommends(
                term.getId(),
                term.getName()
        );
        cacheManager.evict("university:recommend:general:" + term.getId());
        UnivApplyInfoRecommendsResponse responseAfterCacheEvict = generalUnivApplyInfoRecommendService.getGeneralRecommends(
                term.getId(),
                term.getName()
        );

        // then
        assertAll(
                () -> assertThat(cachedResponse.recommendedUniversities())
                        .extracting(UnivApplyInfoPreviewResponse::id)
                        .contains(deletedUnivApplyInfo.getId()),
                () -> assertThat(responseBeforeCacheEvict.recommendedUniversities())
                        .extracting(UnivApplyInfoPreviewResponse::id)
                        .contains(deletedUnivApplyInfo.getId())
                        .doesNotContain(addedUnivApplyInfo.getId()),
                () -> assertThat(responseAfterCacheEvict.recommendedUniversities())
                        .hasSize(RECOMMEND_UNIV_APPLY_INFO_NUM)
                        .extracting(UnivApplyInfoPreviewResponse::id)
                        .contains(addedUnivApplyInfo.getId())
                        .doesNotContain(deletedUnivApplyInfo.getId())
        );
    }
}
