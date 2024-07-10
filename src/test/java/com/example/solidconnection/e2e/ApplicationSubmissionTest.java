package com.example.solidconnection.e2e;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.dto.ScoreRequest;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.config.token.TokenService;
import com.example.solidconnection.config.token.TokenType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.type.VerifyStatus;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static com.example.solidconnection.e2e.DynamicFixture.createSiteUserFixtureByEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("지원 정보 제출 테스트")
class ApplicationSubmissionTest extends BaseEndToEndTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private TokenService tokenService;

    private final String email = "email@email.com";
    private String accessToken;
    private SiteUser siteUser;

    @BeforeEach
    public void setUpUserAndToken() {
        // setUp - 회원 정보 저장
        siteUser = siteUserRepository.save(createSiteUserFixtureByEmail(email));

        // setUp - 엑세스 토큰 생성과 리프레시 토큰 생성 및 저장
        accessToken = tokenService.generateToken(email, TokenType.ACCESS);
        String refreshToken = tokenService.generateToken(email, TokenType.REFRESH);
        tokenService.saveToken(refreshToken, TokenType.REFRESH);
    }

    @Test
    void 대학교_성적과_어학성적을_제출한다() {
        // request - body 생성 및 요청
        ScoreRequest request = new ScoreRequest(LanguageTestType.TOEFL_IBT, "80",
                "languageTestReportUrl", 4.0, 4.5, "gpaReportUrl");
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .contentType("application/json")
                .log().all()
                .post("/application/score")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        Application application = applicationRepository.getApplicationBySiteUser(siteUser);
        assertAll("대학교 성적을 저장한다.",
                () -> assertThat(application.getId()).isNotNull(),
                () -> assertThat(application.getSiteUser().getId()).isEqualTo(siteUser.getId()),
                () -> assertThat(application.getLanguageTest().getLanguageTestType()).isEqualTo(request.languageTestType()),
                () -> assertThat(application.getLanguageTest().getLanguageTestScore()).isEqualTo(request.languageTestScore()),
                () -> assertThat(application.getLanguageTest().getLanguageTestReportUrl()).isEqualTo(request.languageTestReportUrl()),
                () -> assertThat(application.getGpa().getGpa()).isEqualTo(request.gpa()),
                () -> assertThat(application.getGpa().getGpaReportUrl()).isEqualTo(request.gpaReportUrl()),
                () -> assertThat(application.getVerifyStatus()).isEqualTo(VerifyStatus.PENDING),
                () -> assertThat(application.getUpdateCount()).isZero());
    }
}
