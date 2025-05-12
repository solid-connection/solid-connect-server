package com.example.solidconnection.university.service;

import com.example.solidconnection.location.country.domain.InterestedCountry;
import com.example.solidconnection.location.region.domain.InterestedRegion;
import com.example.solidconnection.location.country.repository.InterestedCountryRepository;
import com.example.solidconnection.location.region.repository.InterestedRegionRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponse;
import com.example.solidconnection.university.dto.UniversityRecommendsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.example.solidconnection.university.service.UniversityRecommendService.RECOMMEND_UNIVERSITY_NUM;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("대학교 추천 서비스 테스트")
class UniversityRecommendServiceTest extends BaseIntegrationTest {

    @Autowired
    private UniversityRecommendService universityRecommendService;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private InterestedRegionRepository interestedRegionRepository;

    @Autowired
    private InterestedCountryRepository interestedCountryRepository;

    @Autowired
    private GeneralUniversityRecommendService generalUniversityRecommendService;

    @BeforeEach
    void setUp() {
        generalUniversityRecommendService.init();
    }

    @Test
    void 관심_지역_설정한_사용자의_맞춤_추천_대학을_조회한다() {
        // given
        SiteUser testUser = createSiteUser();
        interestedRegionRepository.save(new InterestedRegion(testUser, 영미권));

        // when
        UniversityRecommendsResponse response = universityRecommendService.getPersonalRecommends(testUser);

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
        SiteUser testUser = createSiteUser();
        interestedCountryRepository.save(new InterestedCountry(testUser, 덴마크));

        // when
        UniversityRecommendsResponse response = universityRecommendService.getPersonalRecommends(testUser);

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
        SiteUser testUser = createSiteUser();
        interestedRegionRepository.save(new InterestedRegion(testUser, 영미권));
        interestedCountryRepository.save(new InterestedCountry(testUser, 덴마크));

        // when
        UniversityRecommendsResponse response = universityRecommendService.getPersonalRecommends(testUser);

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
        // given
        SiteUser testUser = createSiteUser();

        // when
        UniversityRecommendsResponse response = universityRecommendService.getPersonalRecommends(testUser);

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

    private SiteUser createSiteUser() {
        SiteUser siteUser = new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                PreparationStatus.CONSIDERING,
                Role.MENTEE
        );
        return siteUserRepository.save(siteUser);
    }
}
