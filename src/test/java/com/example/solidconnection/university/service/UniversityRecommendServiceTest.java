package com.example.solidconnection.university.service;

import com.example.solidconnection.location.country.domain.InterestedCountry;
import com.example.solidconnection.location.country.fixture.CountryFixture;
import com.example.solidconnection.location.country.repository.InterestedCountyRepository;
import com.example.solidconnection.location.region.domain.InterestedRegion;
import com.example.solidconnection.location.region.fixture.RegionFixture;
import com.example.solidconnection.location.region.repository.InterestedRegionRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponse;
import com.example.solidconnection.university.dto.UniversityRecommendsResponse;
import com.example.solidconnection.university.fixture.UnivApplyInfoFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.example.solidconnection.university.service.UniversityRecommendService.RECOMMEND_UNIVERSITY_NUM;
import static org.assertj.core.api.Assertions.assertThat;

@TestContainerSpringBootTest
@DisplayName("대학교 추천 서비스 테스트")
class UniversityRecommendServiceTest {

    @Autowired
    private UniversityRecommendService universityRecommendService;

    @Autowired
    private InterestedRegionRepository interestedRegionRepository;

    @Autowired
    private InterestedCountyRepository interestedCountyRepository;

    @Autowired
    private GeneralUniversityRecommendService generalUniversityRecommendService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private RegionFixture regionFixture;

    @Autowired
    private CountryFixture countryFixture;

    @Autowired
    private UnivApplyInfoFixture univApplyInfoFixture;

    private SiteUser user;
    private UnivApplyInfo 괌대학_A_지원_정보;
    private UnivApplyInfo 괌대학_B_지원_정보;
    private UnivApplyInfo 네바다주립대학_라스베이거스_지원_정보;
    private UnivApplyInfo 메모리얼대학_세인트존스_A_지원_정보;
    private UnivApplyInfo 서던덴마크대학교_지원_정보;
    private UnivApplyInfo 코펜하겐IT대학_지원_정보;

    @BeforeEach
    void setUp() {
        user = siteUserFixture.사용자();
        괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보();
        괌대학_B_지원_정보 = univApplyInfoFixture.괌대학_B_지원_정보();
        네바다주립대학_라스베이거스_지원_정보 = univApplyInfoFixture.네바다주립대학_라스베이거스_지원_정보();
        메모리얼대학_세인트존스_A_지원_정보 = univApplyInfoFixture.메모리얼대학_세인트존스_A_지원_정보();
        서던덴마크대학교_지원_정보 = univApplyInfoFixture.서던덴마크대학교_지원_정보();
        코펜하겐IT대학_지원_정보 = univApplyInfoFixture.코펜하겐IT대학_지원_정보();
        univApplyInfoFixture.그라츠대학_지원_정보();
        univApplyInfoFixture.그라츠공과대학_지원_정보();
        univApplyInfoFixture.린츠_카톨릭대학_지원_정보();
        univApplyInfoFixture.메이지대학_지원_정보();
        generalUniversityRecommendService.init();
    }

    @Test
    void 관심_지역_설정한_사용자의_맞춤_추천_대학을_조회한다() {
        // given
        interestedRegionRepository.save(new InterestedRegion(user, regionFixture.영미권()));

        // when
        UniversityRecommendsResponse response = universityRecommendService.getPersonalRecommends(user);

        // then
        assertThat(response.recommendedUniversities())
                .hasSize(RECOMMEND_UNIVERSITY_NUM)
                .containsAll(List.of(
                        UniversityInfoForApplyPreviewResponse.from(괌대학_A_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(괌대학_B_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(메모리얼대학_세인트존스_A_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(네바다주립대학_라스베이거스_지원_정보)
                ));
    }

    @Test
    void 관심_국가_설정한_사용자의_맞춤_추천_대학을_조회한다() {
        // given
        interestedCountyRepository.save(new InterestedCountry(user, countryFixture.덴마크()));

        // when
        UniversityRecommendsResponse response = universityRecommendService.getPersonalRecommends(user);

        // then
        assertThat(response.recommendedUniversities())
                .hasSize(RECOMMEND_UNIVERSITY_NUM)
                .containsAll(List.of(
                        UniversityInfoForApplyPreviewResponse.from(서던덴마크대학교_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(코펜하겐IT대학_지원_정보)
                ));
    }

    @Test
    void 관심_지역과_국가_모두_설정한_사용자의_맞춤_추천_대학을_조회한다() {
        // given
        interestedRegionRepository.save(new InterestedRegion(user, regionFixture.영미권()));
        interestedCountyRepository.save(new InterestedCountry(user, countryFixture.덴마크()));

        // when
        UniversityRecommendsResponse response = universityRecommendService.getPersonalRecommends(user);

        // then
        assertThat(response.recommendedUniversities())
                .hasSize(RECOMMEND_UNIVERSITY_NUM)
                .containsExactlyInAnyOrder(
                        UniversityInfoForApplyPreviewResponse.from(괌대학_A_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(괌대학_B_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(메모리얼대학_세인트존스_A_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(네바다주립대학_라스베이거스_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(서던덴마크대학교_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(코펜하겐IT대학_지원_정보)
                );
    }

    @Test
    void 관심사_미설정_사용자는_일반_추천_대학을_조회한다() {
        // when
        UniversityRecommendsResponse response = universityRecommendService.getPersonalRecommends(user);

        // then
        assertThat(response.recommendedUniversities())
                .hasSize(RECOMMEND_UNIVERSITY_NUM)
                .containsExactlyInAnyOrderElementsOf(
                        generalUniversityRecommendService.getRecommendUniversities().stream()
                                .map(UniversityInfoForApplyPreviewResponse::from)
                                .toList()
                );
    }

    @Test
    void 일반_추천_대학을_조회한다() {
        // when
        UniversityRecommendsResponse response = universityRecommendService.getGeneralRecommends();

        // then
        assertThat(response.recommendedUniversities())
                .hasSize(RECOMMEND_UNIVERSITY_NUM)
                .containsExactlyInAnyOrderElementsOf(
                        generalUniversityRecommendService.getRecommendUniversities().stream()
                                .map(UniversityInfoForApplyPreviewResponse::from)
                                .toList()
                );
    }
}
