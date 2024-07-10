package com.example.solidconnection.e2e;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.dto.ScoreRequest;
import com.example.solidconnection.application.dto.UniversityChoiceRequest;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.config.token.TokenService;
import com.example.solidconnection.config.token.TokenType;
import com.example.solidconnection.custom.response.ErrorResponse;
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

import static com.example.solidconnection.application.service.ApplicationSubmissionService.APPLICATION_UPDATE_COUNT_LIMIT;
import static com.example.solidconnection.custom.exception.ErrorCode.APPLY_UPDATE_LIMIT_EXCEED;
import static com.example.solidconnection.custom.exception.ErrorCode.CANT_APPLY_FOR_SAME_UNIVERSITY;
import static com.example.solidconnection.e2e.DynamicFixture.createSiteUserFixtureByEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("지원 정보 제출 테스트")
class ApplicationSubmissionTest extends UniversityDataSetUpEndToEndTest {

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
    void 대학교_성적과_어학성적을_처음으로_제출한다() {
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
        assertAll("대학교 성적과 어학 성적을 저장한다.",
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

    @Test
    void 대학교_성적과_어학성적을_다시_제출한다() {
        // setUp - 성적 정보 저장
        ScoreRequest firstRequest = new ScoreRequest(LanguageTestType.TOEFL_IBT, "80",
                "languageTestReportUrl", 4.0, 4.5, "gpaReportUrl");
        applicationRepository.save(new Application(siteUser, firstRequest.toGpa(), firstRequest.toLanguageTest()));

        // request - body 생성 및 요청
        ScoreRequest secondRequest = new ScoreRequest(LanguageTestType.TOEFL_IBT, "90",
                "languageTestReportUrl", 4.1, 4.5, "gpaReportUrl");
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .body(secondRequest)
                .contentType("application/json")
                .log().all()
                .post("/application/score")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        Application updatedApplication = applicationRepository.getApplicationBySiteUser(siteUser);
        assertAll("대학교 성적과 어학 성적을 수정한다. 이때 수정 횟수는 증가하지 않고, 성적 승인 상태는 PENDING 으로 바뀐다.",
                () -> assertThat(updatedApplication.getId()).isNotNull(),
                () -> assertThat(updatedApplication.getSiteUser().getId()).isEqualTo(siteUser.getId()),
                () -> assertThat(updatedApplication.getLanguageTest().getLanguageTestType()).isEqualTo(secondRequest.languageTestType()),
                () -> assertThat(updatedApplication.getLanguageTest().getLanguageTestScore()).isEqualTo(secondRequest.languageTestScore()),
                () -> assertThat(updatedApplication.getLanguageTest().getLanguageTestReportUrl()).isEqualTo(secondRequest.languageTestReportUrl()),
                () -> assertThat(updatedApplication.getGpa().getGpa()).isEqualTo(secondRequest.gpa()),
                () -> assertThat(updatedApplication.getGpa().getGpaReportUrl()).isEqualTo(secondRequest.gpaReportUrl()),
                () -> assertThat(updatedApplication.getVerifyStatus()).isEqualTo(VerifyStatus.PENDING),
                () -> assertThat(updatedApplication.getUpdateCount()).isZero());
    }

    @Test
    void 성적_제출_후_지망_대학을_제출한다() {
        // setUp - 성적 정보 저장
        ScoreRequest firstRequest = new ScoreRequest(LanguageTestType.TOEFL_IBT, "80",
                "languageTestReportUrl", 4.0, 4.5, "gpaReportUrl");
        applicationRepository.save(new Application(siteUser, firstRequest.toGpa(), firstRequest.toLanguageTest()));

        // request - body 생성 및 요청
        UniversityChoiceRequest request = new UniversityChoiceRequest(그라츠대학_지원_정보.getId(), 코펜하겐IT대학_지원_정보.getId());
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .contentType("application/json")
                .log().all()
                .post("/application/university")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        Application application = applicationRepository.getApplicationBySiteUser(siteUser);
        assertAll("지망 대학교를 저장한다.",
                () -> assertThat(application.getId()).isNotNull(),
                () -> assertThat(application.getSiteUser().getId()).isEqualTo(siteUser.getId()),
                () -> assertThat(application.getFirstChoiceUniversity().getId()).isEqualTo(request.firstChoiceUniversityId()),
                () -> assertThat(application.getSecondChoiceUniversity().getId()).isEqualTo(request.secondChoiceUniversityId()),
                () -> assertThat(application.getNicknameForApply()).isNotNull(),
                () -> assertThat(application.getVerifyStatus()).isEqualTo(VerifyStatus.PENDING),
                () -> assertThat(application.getUpdateCount()).isZero());
    }

    @Test
    void 지망_대학을_수정한다() {
        // setUp - 성적 정보와 지망 대학 저장
        ScoreRequest firstRequest = new ScoreRequest(LanguageTestType.TOEFL_IBT, "80",
                "languageTestReportUrl", 4.0, 4.5, "gpaReportUrl");
        applicationRepository.save(new Application(siteUser, firstRequest.toGpa(), firstRequest.toLanguageTest()))
                .updateUniversityChoice(괌대학_A_지원_정보, 괌대학_B_지원_정보, "nickname");
        Application initialApplication = applicationRepository.getApplicationBySiteUser(siteUser);

        // request - body 생성 및 요청
        UniversityChoiceRequest request = new UniversityChoiceRequest(그라츠대학_지원_정보.getId(), 코펜하겐IT대학_지원_정보.getId());
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .contentType("application/json")
                .log().all()
                .post("/application/university")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        Application updatedApplication = applicationRepository.getApplicationBySiteUser(siteUser);
        assertAll("지망 대학교를 수정한다. 이때 수정 횟수는 증가하고, 성적 승인 상태는 바뀌지 않는다.",
                () -> assertThat(updatedApplication.getId()).isNotNull(),
                () -> assertThat(updatedApplication.getSiteUser().getId()).isEqualTo(siteUser.getId()),
                () -> assertThat(updatedApplication.getFirstChoiceUniversity().getId()).isEqualTo(request.firstChoiceUniversityId()),
                () -> assertThat(updatedApplication.getSecondChoiceUniversity().getId()).isEqualTo(request.secondChoiceUniversityId()),
                () -> assertThat(updatedApplication.getNicknameForApply()).isNotNull(),
                () -> assertThat(updatedApplication.getVerifyStatus()).isEqualTo(initialApplication.getVerifyStatus()),
                () -> assertThat(updatedApplication.getUpdateCount()).isEqualTo(initialApplication.getUpdateCount()));
    }

    @Test
    void 지망_대학을_최대_수정_가능_횟수보다_더_수정하려고하면_예외_응답을_반환한다() {
        // setUp - 성적 정보와 지망 대학 저장
        ScoreRequest firstRequest = new ScoreRequest(LanguageTestType.TOEFL_IBT, "80",
                "languageTestReportUrl", 4.0, 4.5, "gpaReportUrl");
        applicationRepository.save(new Application(siteUser, firstRequest.toGpa(), firstRequest.toLanguageTest()));
        Application initialApplication = applicationRepository.getApplicationBySiteUser(siteUser);

        // setUp - 지망 대학을 한계까지 수정
        for (int i = 0; i <= APPLICATION_UPDATE_COUNT_LIMIT; i++) {
            initialApplication.updateUniversityChoice(괌대학_A_지원_정보, 괌대학_B_지원_정보, "nickname");
            applicationRepository.save(initialApplication);
        }

        // request - body 생성 및 요청
        UniversityChoiceRequest request = new UniversityChoiceRequest(그라츠대학_지원_정보.getId(), 코펜하겐IT대학_지원_정보.getId());
        ErrorResponse errorResponse = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .contentType("application/json")
                .log().all()
                .post("/application/university")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().as(ErrorResponse.class);

        assertThat(errorResponse.message()).isEqualTo(APPLY_UPDATE_LIMIT_EXCEED.getMessage());
    }

    @Test
    void 일지망_대학과_이지망_대학이_같으면_예외_응답을_반환한다() {
        // request - body 생성 및 요청
        UniversityChoiceRequest request = new UniversityChoiceRequest(그라츠대학_지원_정보.getId(), 그라츠대학_지원_정보.getId());
        ErrorResponse errorResponse = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .contentType("application/json")
                .log().all()
                .post("/application/university")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().as(ErrorResponse.class);

        assertThat(errorResponse.message()).isEqualTo(CANT_APPLY_FOR_SAME_UNIVERSITY.getMessage());
    }
}
