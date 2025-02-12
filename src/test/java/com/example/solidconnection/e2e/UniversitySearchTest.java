package com.example.solidconnection.e2e;

import com.example.solidconnection.auth.service.AuthTokenProvider;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponse;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.example.solidconnection.e2e.DynamicFixture.createSiteUserByEmail;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("대학교 검색 테스트")
class UniversitySearchTest extends UniversityDataSetUpEndToEndTest {

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private AuthTokenProvider authTokenProvider;

    private String accessToken;
    private SiteUser siteUser;

    @BeforeEach
    public void setUpUserAndToken() {
        // setUp - 회원 정보 저장
        siteUser = createSiteUserByEmail("email@email.com");
        siteUserRepository.save(siteUser);

        // setUp - 엑세스 토큰 생성과 리프레시 토큰 생성 및 저장
        accessToken = authTokenProvider.generateAccessToken(siteUser);
        authTokenProvider.generateAndSaveRefreshToken(siteUser);
    }

    @Test
    void 아무_필터링_없이_전체_대학을_조회한다() {
        // request - 요청
        List<UniversityInfoForApplyPreviewResponse> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/universities/search")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", UniversityInfoForApplyPreviewResponse.class);

        assertThat(response).containsExactlyInAnyOrder(
                UniversityInfoForApplyPreviewResponse.from(괌대학_A_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(괌대학_B_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(네바다주립대학_라스베이거스_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(메모리얼대학_세인트존스_A_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(서던덴마크대학교_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(코펜하겐IT대학_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(그라츠대학_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(그라츠공과대학_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(린츠_카톨릭대학_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(메이지대학_지원_정보)
        );
    }

    @Test
    void 지역으로_필터링한_대학을_조회한다() {
        // request - 요청
        List<UniversityInfoForApplyPreviewResponse> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/universities/search?region=" + 영미권.getCode())
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", UniversityInfoForApplyPreviewResponse.class);

        assertThat(response).containsExactlyInAnyOrder(
                UniversityInfoForApplyPreviewResponse.from(괌대학_A_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(괌대학_B_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(네바다주립대학_라스베이거스_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(메모리얼대학_세인트존스_A_지원_정보)
        );
    }

    @Test
    void 국가_국문명_또는_대학_국문명으로_필터링한_대학을_조회한다() {
        // request - 요청
        List<UniversityInfoForApplyPreviewResponse> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/universities/search?keyword=라")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", UniversityInfoForApplyPreviewResponse.class);

        assertThat(response).containsExactlyInAnyOrder(
                UniversityInfoForApplyPreviewResponse.from(네바다주립대학_라스베이거스_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(그라츠대학_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(그라츠공과대학_지원_정보)
        );
    }

    @Test
    void 둘_이상의_국가_국문명_또는_대학_국문명으로_필터링한_대학을_조회한다() {
        // request - 요청
        List<UniversityInfoForApplyPreviewResponse> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/universities/search?keyword=라&keyword=일본")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", UniversityInfoForApplyPreviewResponse.class);

        assertThat(response).containsExactlyInAnyOrder(
                UniversityInfoForApplyPreviewResponse.from(네바다주립대학_라스베이거스_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(그라츠대학_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(그라츠공과대학_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(메이지대학_지원_정보)
        );
    }

    @Test
    void 어학시험_종류로_필터링한_대학을_조회한다() {
        // request - 요청
        List<UniversityInfoForApplyPreviewResponse> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/universities/search?testType=TOEFL_IBT")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", UniversityInfoForApplyPreviewResponse.class);

        assertThat(response).containsExactlyInAnyOrder(
                UniversityInfoForApplyPreviewResponse.from(괌대학_A_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(괌대학_B_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(서던덴마크대학교_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(코펜하겐IT대학_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(그라츠대학_지원_정보)
        );
    }

    @Test
    void 어학시험과_시험_성적으로_필터링한_대학을_조회한다() {
        // request - 요청
        List<UniversityInfoForApplyPreviewResponse> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/universities/search?testType=TOEFL_IBT&testScore=70")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", UniversityInfoForApplyPreviewResponse.class);

        assertThat(response).containsExactlyInAnyOrder(
                UniversityInfoForApplyPreviewResponse.from(괌대학_B_지원_정보),
                UniversityInfoForApplyPreviewResponse.from(서던덴마크대학교_지원_정보)
        );
    }

    @Test
    void 지역과_어학시험과_시험_성적으로_필터링한_대학을_조회한다() {
        // request - 요청
        List<UniversityInfoForApplyPreviewResponse> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/universities/search?region=EUROPE&testType=TOEFL_IBT&testScore=70")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", UniversityInfoForApplyPreviewResponse.class);

        assertThat(response)
                .containsExactlyInAnyOrder(UniversityInfoForApplyPreviewResponse.from(서던덴마크대학교_지원_정보));
    }
}
