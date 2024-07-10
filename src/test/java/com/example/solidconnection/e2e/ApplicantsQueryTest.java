package com.example.solidconnection.e2e;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.application.dto.ApplicantResponse;
import com.example.solidconnection.application.dto.ApplicationsResponse;
import com.example.solidconnection.application.dto.UniversityApplicantsResponse;
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

import java.util.List;

import static com.example.solidconnection.e2e.DynamicFixture.createDummyGpa;
import static com.example.solidconnection.e2e.DynamicFixture.createDummyLanguageTest;
import static com.example.solidconnection.e2e.DynamicFixture.createSiteUserByEmail;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지원자 조회 테스트")
class ApplicantsQueryTest extends UniversityDataSetUpEndToEndTest {

    @Autowired
    SiteUserRepository siteUserRepository;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    TokenService tokenService;

    private String accessToken;
    private Application 나의_지원정보;
    private Application 사용자1_지원정보;
    private Application 사용자2_지원정보;
    private Application 사용자3_지원정보;

    @BeforeEach
    public void setUpUserAndToken() {
        // setUp - 회원 정보 저장
        String email = "email@email.com";
        SiteUser siteUser = siteUserRepository.save(createSiteUserByEmail(email));

        // setUp - 엑세스 토큰 생성과 리프레시 토큰 생성 및 저장
        accessToken = tokenService.generateToken(email, TokenType.ACCESS);
        String refreshToken = tokenService.generateToken(email, TokenType.REFRESH);
        tokenService.saveToken(refreshToken, TokenType.REFRESH);

        // setUp - 사용자 정보 저장
        SiteUser 사용자1 = siteUserRepository.save(createSiteUserByEmail("email1"));
        SiteUser 사용자2 = siteUserRepository.save(createSiteUserByEmail("email2"));
        SiteUser 사용자3 = siteUserRepository.save(createSiteUserByEmail("email3"));

        // setUp - 지원 정보 저장
        Gpa gpa = createDummyGpa();
        LanguageTest languageTest = createDummyLanguageTest();
        나의_지원정보 = new Application(siteUser, gpa, languageTest);
        사용자1_지원정보 = new Application(사용자1, gpa, languageTest);
        사용자2_지원정보 = new Application(사용자2, gpa, languageTest);
        사용자3_지원정보 = new Application(사용자3, gpa, languageTest);
        나의_지원정보.updateUniversityChoice(괌대학_B_지원_정보, 괌대학_A_지원_정보, "0");
        사용자1_지원정보.updateUniversityChoice(괌대학_A_지원_정보, 괌대학_B_지원_정보, "1");
        사용자2_지원정보.updateUniversityChoice(메이지대학_지원_정보, 그라츠대학_지원_정보, "2");
        사용자3_지원정보.updateUniversityChoice(네바다주립대학_라스베이거스_지원_정보, 그라츠공과대학_지원_정보, "3");
        나의_지원정보.setVerifyStatus(VerifyStatus.APPROVED);
        사용자1_지원정보.setVerifyStatus(VerifyStatus.APPROVED);
        사용자2_지원정보.setVerifyStatus(VerifyStatus.APPROVED);
        사용자3_지원정보.setVerifyStatus(VerifyStatus.APPROVED);
        applicationRepository.saveAll(List.of(나의_지원정보, 사용자1_지원정보, 사용자2_지원정보, 사용자3_지원정보));
    }

    @Test
    void 전체_지원자를_조회한다() {
        ApplicationsResponse response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().log().all()
                .get("/application")
                .then().log().all()
                .statusCode(200)
                .extract().as(ApplicationsResponse.class);

        List<UniversityApplicantsResponse> firstChoiceApplicants = response.firstChoice();
        List<UniversityApplicantsResponse> secondChoiceApplicants = response.secondChoice();

        assertThat(firstChoiceApplicants).containsAnyElementsOf(List.of(
                UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                        List.of(ApplicantResponse.of(사용자1_지원정보, false))),
                UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                        List.of(ApplicantResponse.of(나의_지원정보, true))),
                UniversityApplicantsResponse.of(메이지대학_지원_정보,
                        List.of(ApplicantResponse.of(사용자2_지원정보, false))),
                UniversityApplicantsResponse.of(네바다주립대학_라스베이거스_지원_정보,
                        List.of(ApplicantResponse.of(사용자3_지원정보, false)))
        ));
        assertThat(secondChoiceApplicants).containsAnyElementsOf(List.of(
                UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                        List.of(ApplicantResponse.of(나의_지원정보, true))),
                UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                        List.of(ApplicantResponse.of(사용자1_지원정보, false))),
                UniversityApplicantsResponse.of(메이지대학_지원_정보,
                        List.of(ApplicantResponse.of(사용자3_지원정보, false))),
                UniversityApplicantsResponse.of(그라츠대학_지원_정보,
                        List.of(ApplicantResponse.of(사용자2_지원정보, false)))
        ));
    }

    @Test
    void 지역으로_필터링해서_지원자를_조회한다() {
        ApplicationsResponse response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().log().all()
                .get("/application?region=" + 영미권.getCode())
                .then().log().all()
                .statusCode(200)
                .extract().as(ApplicationsResponse.class);

        List<UniversityApplicantsResponse> firstChoiceApplicants = response.firstChoice();
        List<UniversityApplicantsResponse> secondChoiceApplicants = response.secondChoice();

        assertThat(firstChoiceApplicants).containsAnyElementsOf(List.of(
                UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                        List.of(ApplicantResponse.of(사용자1_지원정보, false))),
                UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                        List.of(ApplicantResponse.of(나의_지원정보, true))),
                UniversityApplicantsResponse.of(네바다주립대학_라스베이거스_지원_정보,
                        List.of(ApplicantResponse.of(사용자3_지원정보, false)))
        ));
        assertThat(secondChoiceApplicants).containsAnyElementsOf(List.of(
                UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                        List.of(ApplicantResponse.of(나의_지원정보, true))),
                UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                        List.of(ApplicantResponse.of(사용자1_지원정보, false)))
        ));
    }

    @Test
    void 대학_국문_이름으로_필터링해서_지원자를_조회한다() {
        ApplicationsResponse response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().log().all()
                .get("/application?keyword=라")
                .then().log().all()
                .statusCode(200)
                .extract().as(ApplicationsResponse.class);

        List<UniversityApplicantsResponse> firstChoiceApplicants = response.firstChoice();
        List<UniversityApplicantsResponse> secondChoiceApplicants = response.secondChoice();

        assertThat(firstChoiceApplicants).containsExactlyInAnyOrder(
                UniversityApplicantsResponse.of(그라츠대학_지원_정보, List.of()),
                UniversityApplicantsResponse.of(그라츠공과대학_지원_정보, List.of()),
                UniversityApplicantsResponse.of(네바다주립대학_라스베이거스_지원_정보,
                        List.of(ApplicantResponse.of(사용자3_지원정보, false))));
        assertThat(secondChoiceApplicants).containsAnyElementsOf(List.of(
                UniversityApplicantsResponse.of(그라츠대학_지원_정보,
                        List.of(ApplicantResponse.of(사용자2_지원정보, false))),
                UniversityApplicantsResponse.of(그라츠공과대학_지원_정보,
                        List.of(ApplicantResponse.of(사용자3_지원정보, false))),
                UniversityApplicantsResponse.of(네바다주립대학_라스베이거스_지원_정보, List.of())));
    }

    @Test
    void 국가_국문_이름으로_필터링해서_지원자를_조회한다() {
        ApplicationsResponse response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().log().all()
                .get("/application?keyword=일본")
                .then().log().all()
                .statusCode(200)
                .extract().as(ApplicationsResponse.class);

        List<UniversityApplicantsResponse> firstChoiceApplicants = response.firstChoice();
        List<UniversityApplicantsResponse> secondChoiceApplicants = response.secondChoice();

        assertThat(firstChoiceApplicants).containsExactlyInAnyOrder(
                UniversityApplicantsResponse.of(메이지대학_지원_정보,
                        List.of(ApplicantResponse.of(사용자2_지원정보, false))));
        assertThat(secondChoiceApplicants).containsExactlyInAnyOrder(
                UniversityApplicantsResponse.of(메이지대학_지원_정보, List.of()));
    }
}