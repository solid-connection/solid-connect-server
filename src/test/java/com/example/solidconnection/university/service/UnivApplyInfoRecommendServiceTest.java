package com.example.solidconnection.university.service;

import static com.example.solidconnection.university.service.UnivApplyInfoRecommendService.RECOMMEND_UNIV_APPLY_INFO_NUM;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.solidconnection.location.country.domain.InterestedCountry;
import com.example.solidconnection.location.country.fixture.CountryFixture;
import com.example.solidconnection.location.country.repository.InterestedCountryRepository;
import com.example.solidconnection.location.region.domain.InterestedRegion;
import com.example.solidconnection.location.region.fixture.RegionFixture;
import com.example.solidconnection.location.region.repository.InterestedRegionRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.term.domain.Term;
import com.example.solidconnection.term.fixture.TermFixture;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoRecommendsResponse;
import com.example.solidconnection.university.fixture.UnivApplyInfoFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("대학 지원 정보 추천 서비스 테스트")
class UnivApplyInfoRecommendServiceTest {

    @Autowired
    private UnivApplyInfoRecommendService univApplyInfoRecommendService;

    @Autowired
    private InterestedRegionRepository interestedRegionRepository;

    @Autowired
    private InterestedCountryRepository interestedCountryRepository;

    @Autowired
    private GeneralUnivApplyInfoRecommendService generalUnivApplyInfoRecommendService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private RegionFixture regionFixture;

    @Autowired
    private CountryFixture countryFixture;

    @Autowired
    private UnivApplyInfoFixture univApplyInfoFixture;

    @Autowired
    private TermFixture termFixture;

    private SiteUser user;
    private Term term;
    private UnivApplyInfo 괌대학_A_지원_정보;
    private UnivApplyInfo 괌대학_B_지원_정보;
    private UnivApplyInfo 네바다주립대학_라스베이거스_지원_정보;
    private UnivApplyInfo 메모리얼대학_세인트존스_A_지원_정보;
    private UnivApplyInfo 서던덴마크대학교_지원_정보;
    private UnivApplyInfo 코펜하겐IT대학_지원_정보;

    @BeforeEach
    void setUp() {
        term = termFixture.현재_학기("2025-2");
        user = siteUserFixture.사용자();
        괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보(term.getId());
        괌대학_B_지원_정보 = univApplyInfoFixture.괌대학_B_지원_정보(term.getId());
        네바다주립대학_라스베이거스_지원_정보 = univApplyInfoFixture.네바다주립대학_라스베이거스_지원_정보(term.getId());
        메모리얼대학_세인트존스_A_지원_정보 = univApplyInfoFixture.메모리얼대학_세인트존스_A_지원_정보(term.getId());
        서던덴마크대학교_지원_정보 = univApplyInfoFixture.서던덴마크대학교_지원_정보(term.getId());
        코펜하겐IT대학_지원_정보 = univApplyInfoFixture.코펜하겐IT대학_지원_정보(term.getId());
        univApplyInfoFixture.그라츠대학_지원_정보(term.getId());
        univApplyInfoFixture.그라츠공과대학_지원_정보(term.getId());
        univApplyInfoFixture.린츠_카톨릭대학_지원_정보(term.getId());
        univApplyInfoFixture.메이지대학_지원_정보(term.getId());
        generalUnivApplyInfoRecommendService.init();
    }

    @Test
    void 관심_지역_설정한_사용자의_맞춤_추천_대학_지원_정보를_조회한다() {
        // given
        interestedRegionRepository.save(new InterestedRegion(user, regionFixture.영미권()));

        // when
        UnivApplyInfoRecommendsResponse response = univApplyInfoRecommendService.getPersonalRecommends(user.getId());

        // then
        assertThat(response.recommendedUniversities())
                .hasSize(RECOMMEND_UNIV_APPLY_INFO_NUM)
                .containsAll(List.of(
                        UnivApplyInfoPreviewResponse.of(괌대학_A_지원_정보, term.getName(), null),
                        UnivApplyInfoPreviewResponse.of(괌대학_B_지원_정보, term.getName(), null),
                        UnivApplyInfoPreviewResponse.of(메모리얼대학_세인트존스_A_지원_정보, term.getName(), null),
                        UnivApplyInfoPreviewResponse.of(네바다주립대학_라스베이거스_지원_정보, term.getName(), null)
                ));
    }

    @Test
    void 관심_국가_설정한_사용자의_맞춤_추천_대학_지원_정보를_조회한다() {
        // given
        interestedCountryRepository.save(new InterestedCountry(user, countryFixture.덴마크()));

        // when
        UnivApplyInfoRecommendsResponse response = univApplyInfoRecommendService.getPersonalRecommends(user.getId());

        // then
        assertThat(response.recommendedUniversities())
                .hasSize(RECOMMEND_UNIV_APPLY_INFO_NUM)
                .containsAll(List.of(
                        UnivApplyInfoPreviewResponse.of(서던덴마크대학교_지원_정보, term.getName(), null),
                        UnivApplyInfoPreviewResponse.of(코펜하겐IT대학_지원_정보, term.getName(), null)
                ));
    }

    @Test
    void 관심_지역과_국가_모두_설정한_사용자의_맞춤_추천_대학_지원_정보를_조회한다() {
        // given
        interestedRegionRepository.save(new InterestedRegion(user, regionFixture.영미권()));
        interestedCountryRepository.save(new InterestedCountry(user, countryFixture.덴마크()));

        // when
        UnivApplyInfoRecommendsResponse response = univApplyInfoRecommendService.getPersonalRecommends(user.getId());

        // then
        assertThat(response.recommendedUniversities())
                .hasSize(RECOMMEND_UNIV_APPLY_INFO_NUM)
                .containsExactlyInAnyOrder(
                        UnivApplyInfoPreviewResponse.of(괌대학_A_지원_정보, term.getName(), null),
                        UnivApplyInfoPreviewResponse.of(괌대학_B_지원_정보, term.getName(), null),
                        UnivApplyInfoPreviewResponse.of(메모리얼대학_세인트존스_A_지원_정보, term.getName(), null),
                        UnivApplyInfoPreviewResponse.of(네바다주립대학_라스베이거스_지원_정보, term.getName(), null),
                        UnivApplyInfoPreviewResponse.of(서던덴마크대학교_지원_정보, term.getName(), null),
                        UnivApplyInfoPreviewResponse.of(코펜하겐IT대학_지원_정보, term.getName(), null)
                );
    }

    @Test
    void 관심사_미설정_사용자는_일반_추천_대학_지원_정보를_조회한다() {
        // when
        UnivApplyInfoRecommendsResponse response = univApplyInfoRecommendService.getPersonalRecommends(user.getId());

        // then
        assertThat(response.recommendedUniversities())
                .hasSize(RECOMMEND_UNIV_APPLY_INFO_NUM)
                .containsExactlyInAnyOrderElementsOf(
                        generalUnivApplyInfoRecommendService.getGeneralRecommends().stream()
                                .map(univApplyInfo -> UnivApplyInfoPreviewResponse.of(
                                        univApplyInfo,
                                        term.getName(),
                                        null
                                ))
                                .toList()
                );
    }

    @Test
    void 일반_추천_대학_지원_정보를_조회한다() {
        // when
        UnivApplyInfoRecommendsResponse response = univApplyInfoRecommendService.getGeneralRecommends();

        // then
        assertThat(response.recommendedUniversities())
                .hasSize(RECOMMEND_UNIV_APPLY_INFO_NUM)
                .containsExactlyInAnyOrderElementsOf(
                        generalUnivApplyInfoRecommendService.getGeneralRecommends().stream()
                                .map(univApplyInfo -> UnivApplyInfoPreviewResponse.of(
                                        univApplyInfo,
                                        term.getName(),
                                        null
                                ))
                                .toList()
                );
    }
}
