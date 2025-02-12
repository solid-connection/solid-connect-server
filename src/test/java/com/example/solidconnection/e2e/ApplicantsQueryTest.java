package com.example.solidconnection.e2e;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.application.dto.ApplicantResponse;
import com.example.solidconnection.application.dto.ApplicationsResponse;
import com.example.solidconnection.application.dto.UniversityApplicantsResponse;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.auth.service.AuthTokenProvider;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.VerifyStatus;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

import static com.example.solidconnection.e2e.DynamicFixture.createDummyGpa;
import static com.example.solidconnection.e2e.DynamicFixture.createDummyLanguageTest;
import static com.example.solidconnection.e2e.DynamicFixture.createSiteUserByEmail;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지원자 조회 테스트")
class ApplicantsQueryTest extends UniversityDataSetUpEndToEndTest {

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AuthTokenProvider authTokenProvider;

    private String accessToken;
    private String adminAccessToken;
    private String user6AccessToken;
    private Application 나의_지원정보;
    private Application 사용자1_지원정보;
    private Application 사용자2_지원정보;
    private Application 사용자3_지원정보;
    private Application 사용자4_이전학기_지원정보;
    private Application 사용자5_관리자_지원정보;
    private Application 사용자6_지원정보;

    @Value("${university.term}")
    private String term;
    private String beforeTerm = "1988-1";

    @BeforeEach
    public void setUpUserAndToken() {
        // setUp - 사용자 정보 저장
        SiteUser 나 = siteUserRepository.save(createSiteUserByEmail("my-email"));
        SiteUser 사용자1 = siteUserRepository.save(createSiteUserByEmail("email1"));
        SiteUser 사용자2 = siteUserRepository.save(createSiteUserByEmail("email2"));
        SiteUser 사용자3 = siteUserRepository.save(createSiteUserByEmail("email3"));
        SiteUser 사용자4_이전학기_지원자 = siteUserRepository.save(createSiteUserByEmail("email4"));
        SiteUser 사용자5_관리자 = siteUserRepository.save(createSiteUserByEmail("email5"));
        SiteUser 사용자6 = siteUserRepository.save(createSiteUserByEmail("email6"));

        // setUp - 엑세스 토큰 생성과 리프레시 토큰 생성 및 저장
        accessToken = authTokenProvider.generateAccessToken(나);
        authTokenProvider.generateAndSaveRefreshToken(나);

        adminAccessToken = authTokenProvider.generateAccessToken(사용자5_관리자);
        authTokenProvider.generateAndSaveRefreshToken(사용자5_관리자);

        user6AccessToken = authTokenProvider.generateAccessToken(사용자6);
        authTokenProvider.generateAndSaveRefreshToken(사용자6);

        // setUp - 지원 정보 저장
        Gpa gpa = createDummyGpa();
        LanguageTest languageTest = createDummyLanguageTest();
        나의_지원정보 = new Application(나, gpa, languageTest, term);
        사용자1_지원정보 = new Application(사용자1, gpa, languageTest, term);
        사용자2_지원정보 = new Application(사용자2, gpa, languageTest, term);
        사용자3_지원정보 = new Application(사용자3, gpa, languageTest, term);
        사용자4_이전학기_지원정보 = new Application(사용자4_이전학기_지원자, gpa, languageTest, beforeTerm);
        사용자5_관리자_지원정보 = new Application(사용자5_관리자, gpa, languageTest, term);
        사용자6_지원정보 = new Application(사용자6, gpa, languageTest, term);

        나의_지원정보.updateUniversityChoice(괌대학_B_지원_정보, 괌대학_A_지원_정보, 린츠_카톨릭대학_지원_정보, "0");
        사용자1_지원정보.updateUniversityChoice(괌대학_A_지원_정보, 괌대학_B_지원_정보, 그라츠공과대학_지원_정보, "1");
        사용자2_지원정보.updateUniversityChoice(메이지대학_지원_정보, 그라츠대학_지원_정보, 서던덴마크대학교_지원_정보, "2");
        사용자3_지원정보.updateUniversityChoice(네바다주립대학_라스베이거스_지원_정보, 그라츠공과대학_지원_정보, 메이지대학_지원_정보, "3");
        사용자4_이전학기_지원정보.updateUniversityChoice(네바다주립대학_라스베이거스_지원_정보, 그라츠공과대학_지원_정보, 메이지대학_지원_정보, "4");
        사용자6_지원정보.updateUniversityChoice(코펜하겐IT대학_지원_정보, null, null, "6");
        나의_지원정보.setVerifyStatus(VerifyStatus.APPROVED);
        사용자1_지원정보.setVerifyStatus(VerifyStatus.APPROVED);
        사용자2_지원정보.setVerifyStatus(VerifyStatus.APPROVED);
        사용자3_지원정보.setVerifyStatus(VerifyStatus.APPROVED);
        사용자4_이전학기_지원정보.setVerifyStatus(VerifyStatus.APPROVED);
        사용자5_관리자_지원정보.setVerifyStatus(VerifyStatus.APPROVED);
        사용자6_지원정보.setVerifyStatus(VerifyStatus.APPROVED);
        applicationRepository.saveAll(List.of(나의_지원정보, 사용자1_지원정보, 사용자2_지원정보, 사용자3_지원정보, 사용자4_이전학기_지원정보, 사용자5_관리자_지원정보, 사용자6_지원정보));
    }

    @Test
    void 전체_지원자를_조회한다() {
        ApplicationsResponse response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().log().all()
                .get("/applications")
                .then().log().all()
                .statusCode(200)
                .extract().as(ApplicationsResponse.class);

        List<UniversityApplicantsResponse> firstChoiceApplicants = response.firstChoice();
        List<UniversityApplicantsResponse> secondChoiceApplicants = response.secondChoice();
        List<UniversityApplicantsResponse> thirdChoiceApplicants = response.thirdChoice();

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
        assertThat(thirdChoiceApplicants).containsAnyElementsOf(List.of(
                UniversityApplicantsResponse.of(린츠_카톨릭대학_지원_정보,
                        List.of(ApplicantResponse.of(나의_지원정보, true))),
                UniversityApplicantsResponse.of(서던덴마크대학교_지원_정보,
                        List.of(ApplicantResponse.of(사용자2_지원정보, false))),
                UniversityApplicantsResponse.of(그라츠공과대학_지원_정보,
                        List.of(ApplicantResponse.of(사용자1_지원정보, false))),
                UniversityApplicantsResponse.of(메이지대학_지원_정보,
                        List.of(ApplicantResponse.of(사용자3_지원정보, false)))
        ));
    }

    @Test
    void 지역으로_필터링해서_지원자를_조회한다() {
        ApplicationsResponse response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().log().all()
                .get("/applications?region=" + 영미권.getCode())
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
                        List.of(ApplicantResponse.of(사용자3_지원정보, false)))));
        assertThat(secondChoiceApplicants).containsAnyElementsOf(List.of(
                UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                        List.of(ApplicantResponse.of(나의_지원정보, true))),
                UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                        List.of(ApplicantResponse.of(사용자1_지원정보, false)))));
    }

    @Test
    void 대학_국문_이름으로_필터링해서_지원자를_조회한다() {
        ApplicationsResponse response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().log().all()
                .get("/applications?keyword=라")
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
                .get("/applications?keyword=일본")
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

    @Test
    void 지원자를_조회할_때_이전학기_지원자는_조회되지_않는다() {
        ApplicationsResponse response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().log().all()
                .get("/applications")
                .then().log().all()
                .statusCode(200)
                .extract().as(ApplicationsResponse.class);

        List<UniversityApplicantsResponse> firstChoiceApplicants = response.firstChoice();
        List<UniversityApplicantsResponse> secondChoiceApplicants = response.secondChoice();
        List<UniversityApplicantsResponse> thirdChoiceApplicants = response.thirdChoice();


        assertThat(firstChoiceApplicants).doesNotContainAnyElementsOf(List.of(
                UniversityApplicantsResponse.of(네바다주립대학_라스베이거스_지원_정보,
                        List.of(ApplicantResponse.of(사용자4_이전학기_지원정보, false)))
        ));
        assertThat(secondChoiceApplicants).doesNotContainAnyElementsOf(List.of(
                UniversityApplicantsResponse.of(네바다주립대학_라스베이거스_지원_정보,
                        List.of(ApplicantResponse.of(사용자4_이전학기_지원정보, false)))
        ));
        assertThat(thirdChoiceApplicants).doesNotContainAnyElementsOf(List.of(
                UniversityApplicantsResponse.of(네바다주립대학_라스베이거스_지원_정보,
                        List.of(ApplicantResponse.of(사용자4_이전학기_지원정보, false)))
        ));
    }

    @Test
    void 경쟁자를_조회한다() {
        ApplicationsResponse response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().log().all()
                .get("/applications/competitors")
                .then().log().all()
                .statusCode(200)
                .extract().as(ApplicationsResponse.class);

        Integer choicedUniversityCount = 3;

        List<UniversityApplicantsResponse> firstChoiceApplicants = response.firstChoice();
        List<UniversityApplicantsResponse> secondChoiceApplicants = response.secondChoice();
        List<UniversityApplicantsResponse> thirdChoiceApplicants = response.thirdChoice();

        assertThat(firstChoiceApplicants).containsExactlyInAnyOrder(
                UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                        List.of(ApplicantResponse.of(사용자1_지원정보, false))),
                UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                        List.of(ApplicantResponse.of(나의_지원정보, true))),
                UniversityApplicantsResponse.of(린츠_카톨릭대학_지원_정보, List.of()));
        assertThat(secondChoiceApplicants).containsExactlyInAnyOrder(
                UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                        List.of(ApplicantResponse.of(나의_지원정보, true))),
                UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                        List.of(ApplicantResponse.of(사용자1_지원정보, false))),
                UniversityApplicantsResponse.of(린츠_카톨릭대학_지원_정보,
                        List.of()));
        assertThat(thirdChoiceApplicants).containsExactlyInAnyOrder(
                UniversityApplicantsResponse.of(괌대학_A_지원_정보,
                        List.of()),
                UniversityApplicantsResponse.of(괌대학_B_지원_정보,
                        List.of()),
                UniversityApplicantsResponse.of(린츠_카톨릭대학_지원_정보,
                        List.of(ApplicantResponse.of(나의_지원정보, true))));

        assertThat(firstChoiceApplicants.size()).isEqualTo(choicedUniversityCount);
        assertThat(secondChoiceApplicants.size()).isEqualTo(choicedUniversityCount);
        assertThat(thirdChoiceApplicants.size()).isEqualTo(choicedUniversityCount);
    }

    @Test
    void 지원_대학중_미선택이_있을_떄_경쟁자를_조회한다() {
        ApplicationsResponse response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + user6AccessToken)
                .when().log().all()
                .get("/applications/competitors")
                .then().log().all()
                .statusCode(200)
                .extract().as(ApplicationsResponse.class);

        Integer choicedUniversityCount = 1;

        List<UniversityApplicantsResponse> firstChoiceApplicants = response.firstChoice();
        List<UniversityApplicantsResponse> secondChoiceApplicants = response.secondChoice();
        List<UniversityApplicantsResponse> thirdChoiceApplicants = response.thirdChoice();

        assertThat(firstChoiceApplicants.size()).isEqualTo(choicedUniversityCount);
        assertThat(secondChoiceApplicants.size()).isEqualTo(choicedUniversityCount);
        assertThat(thirdChoiceApplicants.size()).isEqualTo(choicedUniversityCount);
    }

    @Test
    void 지원_대학이_모두_미선택일_때_경쟁자를_조회한다() {
        ApplicationsResponse response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + adminAccessToken)
                .when().log().all()
                .get("/applications/competitors")
                .then().log().all()
                .statusCode(200)
                .extract().as(ApplicationsResponse.class);

        Integer choicedUniversityCount = 0;

        List<UniversityApplicantsResponse> firstChoiceApplicants = response.firstChoice();
        List<UniversityApplicantsResponse> secondChoiceApplicants = response.secondChoice();
        List<UniversityApplicantsResponse> thirdChoiceApplicants = response.thirdChoice();

        assertThat(firstChoiceApplicants.size()).isEqualTo(choicedUniversityCount);
        assertThat(secondChoiceApplicants.size()).isEqualTo(choicedUniversityCount);
        assertThat(thirdChoiceApplicants.size()).isEqualTo(choicedUniversityCount);
    }
}
