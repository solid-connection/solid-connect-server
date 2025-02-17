package com.example.solidconnection.e2e;

import com.example.solidconnection.auth.service.AuthTokenProvider;
import com.example.solidconnection.entity.InterestedCountry;
import com.example.solidconnection.entity.InterestedRegion;
import com.example.solidconnection.repositories.InterestedCountyRepository;
import com.example.solidconnection.repositories.InterestedRegionRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponse;
import com.example.solidconnection.university.dto.UniversityRecommendsResponse;
import com.example.solidconnection.university.service.GeneralUniversityRecommendService;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;

import static com.example.solidconnection.e2e.DynamicFixture.createSiteUserByEmail;
import static com.example.solidconnection.university.service.UniversityRecommendService.RECOMMEND_UNIVERSITY_NUM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("추천 대학 목록 조회 테스트")
class UniversityRecommendTest extends UniversityDataSetUpEndToEndTest {

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private InterestedRegionRepository interestedRegionRepository;

    @Autowired
    private InterestedCountyRepository interestedCountyRepository;

    @Autowired
    private AuthTokenProvider authTokenProvider;

    @Autowired
    private GeneralUniversityRecommendService generalUniversityRecommendService;

    private SiteUser siteUser;
    private String accessToken;

    @BeforeEach
    void setUp() {
        // setUp - 회원 정보 저장
        String email = "email@email.com";
        siteUser = siteUserRepository.save(createSiteUserByEmail(email));
        generalUniversityRecommendService.init();

        // setUp - 엑세스 토큰 생성과 리프레시 토큰 생성 및 저장
        accessToken = authTokenProvider.generateAccessToken(siteUser);
        authTokenProvider.generateAndSaveRefreshToken(siteUser);
    }

    @Test
    void 관심_지역을_설정한_사용자의_추천_대학_목록을_조회한다() {
        // setUp -  관심 지역 저장
        interestedRegionRepository.save(new InterestedRegion(siteUser, 영미권));

        // request - 요청
        UniversityRecommendsResponse response = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .log().all()
                .get("/universities/recommend")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(UniversityRecommendsResponse.class);

        assertAll(
                String.format("관심 지역에 해당하는 학교를 포함한 %d개의 대학 목록을 반환한다.", RECOMMEND_UNIVERSITY_NUM),
                () -> assertThat(response.recommendedUniversities())
                        .hasSize(RECOMMEND_UNIVERSITY_NUM),
                () -> assertThat(response.recommendedUniversities())
                        .containsOnlyOnceElementsOf(List.of(
                                UniversityInfoForApplyPreviewResponse.from(괌대학_A_지원_정보),
                                UniversityInfoForApplyPreviewResponse.from(괌대학_B_지원_정보),
                                UniversityInfoForApplyPreviewResponse.from(메모리얼대학_세인트존스_A_지원_정보),
                                UniversityInfoForApplyPreviewResponse.from(네바다주립대학_라스베이거스_지원_정보)
                        ))
        );
    }

    @Test
    void 관심_국가를_설정한_사용자의_추천_대학_목록을_조회한다() {
        // setUp - 관심 국가 저장
        interestedCountyRepository.save(new InterestedCountry(siteUser, 덴마크));

        // request - 요청
        UniversityRecommendsResponse response = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .log().all()
                .get("/universities/recommend")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(UniversityRecommendsResponse.class);

        assertAll(
                String.format("관심 국가에 해당하는 학교를 포함한 %d개의 대학 목록을 반환한다.", RECOMMEND_UNIVERSITY_NUM),
                () -> assertThat(response.recommendedUniversities())
                        .hasSize(RECOMMEND_UNIVERSITY_NUM),
                () -> assertThat(response.recommendedUniversities())
                        .containsOnlyOnceElementsOf(List.of(
                                UniversityInfoForApplyPreviewResponse.from(서던덴마크대학교_지원_정보),
                                UniversityInfoForApplyPreviewResponse.from(코펜하겐IT대학_지원_정보)
                        ))
        );
    }

    @Test
    void 관심_지역과_관심_국가를_설정한_사용자의_추천_대학_목록을_조회한다() {
        // setUp - 관심 지역과 국가 저장
        interestedRegionRepository.save(new InterestedRegion(siteUser, 영미권));
        interestedCountyRepository.save(new InterestedCountry(siteUser, 덴마크));

        // request - 요청
        UniversityRecommendsResponse response = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .log().all()
                .get("/universities/recommend")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(UniversityRecommendsResponse.class);

        assertAll(
                String.format("관심 지역 또는 국가에 해당하는 학교를 포함한 %d개의 대학 목록을 반환한다.", RECOMMEND_UNIVERSITY_NUM),
                () -> assertThat(response.recommendedUniversities())
                        .hasSize(RECOMMEND_UNIVERSITY_NUM),
                () -> assertThat(response.recommendedUniversities())
                        .containsOnlyOnceElementsOf(List.of(
                                UniversityInfoForApplyPreviewResponse.from(괌대학_A_지원_정보),
                                UniversityInfoForApplyPreviewResponse.from(괌대학_B_지원_정보),
                                UniversityInfoForApplyPreviewResponse.from(메모리얼대학_세인트존스_A_지원_정보),
                                UniversityInfoForApplyPreviewResponse.from(네바다주립대학_라스베이거스_지원_정보),
                                UniversityInfoForApplyPreviewResponse.from(서던덴마크대학교_지원_정보),
                                UniversityInfoForApplyPreviewResponse.from(코펜하겐IT대학_지원_정보)
                        ))
        );
    }

    @Test
    void 관심_지역_또는_관심_국가를_설정하지_않은_사용자의_추천_대학_목록을_조회한다() {
        // request - 요청
        UniversityRecommendsResponse response = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .log().all()
                .get("/universities/recommend")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(UniversityRecommendsResponse.class);

        List<UniversityInfoForApplyPreviewResponse> generalRecommendUniversities
                = this.generalUniversityRecommendService.getRecommendUniversities().stream()
                .map(UniversityInfoForApplyPreviewResponse::from)
                .toList();
        assertAll(
                String.format("일반 추천 대학 목록 %d개를 반환한다.", RECOMMEND_UNIVERSITY_NUM),
                () -> assertThat(response.recommendedUniversities())
                        .hasSize(RECOMMEND_UNIVERSITY_NUM),
                () -> assertThat(generalRecommendUniversities)
                        .containsOnlyOnceElementsOf(response.recommendedUniversities())
        );
    }

    @Test
    void 로그인하지_않은_방문객의_추천_대학_목록을_조회한다() {
        // request - 요청
        UniversityRecommendsResponse response = RestAssured.given()
                .log().all()
                .get("/universities/recommend")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(UniversityRecommendsResponse.class);

        List<UniversityInfoForApplyPreviewResponse> generalRecommendUniversities
                = this.generalUniversityRecommendService.getRecommendUniversities().stream()
                .map(UniversityInfoForApplyPreviewResponse::from)
                .toList();
        assertAll(
                String.format("일반 추천 대학 목록 %d개를 반환한다.", RECOMMEND_UNIVERSITY_NUM),
                () -> assertThat(response.recommendedUniversities())
                        .hasSize(RECOMMEND_UNIVERSITY_NUM),
                () -> assertThat(generalRecommendUniversities)
                        .containsOnlyOnceElementsOf(response.recommendedUniversities())
        );
    }
}
