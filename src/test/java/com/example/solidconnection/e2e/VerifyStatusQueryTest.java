package com.example.solidconnection.e2e;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.dto.VerifyStatusResponse;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.config.token.TokenService;
import com.example.solidconnection.config.token.TokenType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.VerifyStatus;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.example.solidconnection.e2e.DynamicFixture.createDummyGpa;
import static com.example.solidconnection.e2e.DynamicFixture.createDummyLanguageTest;
import static com.example.solidconnection.e2e.DynamicFixture.createSiteUserByEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("지원 상태 조회 테스트")
class VerifyStatusQueryTest extends UniversityDataSetUpEndToEndTest {

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ApplicationRepository applicationRepository;

    private String accessToken;
    private SiteUser siteUser;

    @BeforeEach
    public void setUpUserAndToken() {
        // setUp - 회원 정보 저장
        String email = "email@email.com";
        siteUser = siteUserRepository.save(createSiteUserByEmail(email));

        // setUp - 엑세스 토큰 생성과 리프레시 토큰 생성 및 저장
        accessToken = tokenService.generateToken(email, TokenType.ACCESS);
        String refreshToken = tokenService.generateToken(email, TokenType.REFRESH);
        tokenService.saveToken(refreshToken, TokenType.REFRESH);
    }

    @Test
    void 아무것도_제출하지_않은_상태를_반환한다() {
        // request - 요청
        VerifyStatusResponse response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/application/status")
                .then().log().all()
                .statusCode(200)
                .extract().as(VerifyStatusResponse.class);

        assertAll(
                () -> assertThat(response.status()).isEqualTo("NOT_SUBMITTED"),
                () -> assertThat(response.updateCount()).isZero()
        );
    }

    @Test
    void 성적만_제출한_상태를_반환한다() {
        // setUp - 성적만 제출한 상태
        Application application = new Application(siteUser, createDummyGpa(), createDummyLanguageTest());
        applicationRepository.save(application);

        // request - 요청
        VerifyStatusResponse response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/application/status")
                .then().log().all()
                .statusCode(200)
                .extract().as(VerifyStatusResponse.class);

        assertAll(
                () -> assertThat(response.status()).isEqualTo("SCORE_SUBMITTED"),
                () -> assertThat(response.updateCount()).isZero()
        );
    }

    @Test
    void 성적과_대학을_모두_제출하고_승인을_기대라는_상태를_반환한다() {
        // setUp - 성적과 대학을 모두 제출한 상태
        Application application = new Application(siteUser, createDummyGpa(), createDummyLanguageTest());
        application.updateUniversityChoice(괌대학_B_지원_정보, 괌대학_A_지원_정보, 네바다주립대학_라스베이거스_지원_정보, "닉네임");
        applicationRepository.save(application);

        // request - 요청
        VerifyStatusResponse response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/application/status")
                .then().log().all()
                .statusCode(200)
                .extract().as(VerifyStatusResponse.class);

        assertAll(
                () -> assertThat(response.status()).isEqualTo("SUBMITTED_PENDING"),
                () -> assertThat(response.updateCount()).isZero()
        );
    }

    @Test
    void 성적과_대학을_모두_제출했지만_승인이_반려된_상태를_반환한다() {
        // setUp - 성적과 대학을 모두 제출했지만, 승인 거절
        Application application = new Application(siteUser, createDummyGpa(), createDummyLanguageTest());
        application.updateUniversityChoice(괌대학_B_지원_정보, 괌대학_A_지원_정보, 네바다주립대학_라스베이거스_지원_정보,"닉네임");
        application.setVerifyStatus(VerifyStatus.REJECTED);
        applicationRepository.save(application);

        // request - 요청
        VerifyStatusResponse response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/application/status")
                .then().log().all()
                .statusCode(200)
                .extract().as(VerifyStatusResponse.class);

        assertAll(
                () -> assertThat(response.status()).isEqualTo("SUBMITTED_REJECTED"),
                () -> assertThat(response.updateCount()).isZero()
        );
    }

    @Test
    void 성적과_대학을_모두_제출했으며_승인이_된_상태를_반환한다() {
        // setUp - 성적과 대학을 모두 제출했으며, 승인이 된 상태
        Application application = new Application(siteUser, createDummyGpa(), createDummyLanguageTest());
        application.updateUniversityChoice(괌대학_B_지원_정보, 괌대학_A_지원_정보, 네바다주립대학_라스베이거스_지원_정보, "닉네임");
        application.setVerifyStatus(VerifyStatus.APPROVED);
        applicationRepository.save(application);

        // request - 요청
        VerifyStatusResponse response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/application/status")
                .then().log().all()
                .statusCode(200)
                .extract().as(VerifyStatusResponse.class);

        assertAll(
                () -> assertThat(response.status()).isEqualTo("SUBMITTED_APPROVED"),
                () -> assertThat(response.updateCount()).isZero()
        );
    }
}
