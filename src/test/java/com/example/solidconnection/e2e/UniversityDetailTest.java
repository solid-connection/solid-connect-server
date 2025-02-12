package com.example.solidconnection.e2e;

import com.example.solidconnection.auth.service.AuthTokenProvider;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.university.dto.LanguageRequirementResponse;
import com.example.solidconnection.university.dto.UniversityDetailResponse;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static com.example.solidconnection.e2e.DynamicFixture.createSiteUserByEmail;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("대학교 상세 조회 테스트")
class UniversityDetailTest extends UniversityDataSetUpEndToEndTest {

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private AuthTokenProvider authTokenProvider;

    private String accessToken;

    @BeforeEach
    public void setUpUserAndToken() {
        // setUp - 회원 정보 저장
        String email = "email@email.com";
        SiteUser siteUser = createSiteUserByEmail(email);
        siteUserRepository.save(siteUser);

        // setUp - 엑세스 토큰 생성과 리프레시 토큰 생성 및 저장
        accessToken = authTokenProvider.generateAccessToken(siteUser);
        authTokenProvider.generateAndSaveRefreshToken(siteUser);
    }

    @Test
    void 대학교_정보를_조회한다() {
        // request - 요청
        UniversityDetailResponse response = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .log().all()
                .get("/universities/" + 메이지대학_지원_정보.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(UniversityDetailResponse.class);

        // response - 응답
        Assertions.assertAll(
                () -> assertThat(response.id()).isEqualTo(메이지대학_지원_정보.getId()),
                () -> assertThat(response.koreanName()).isEqualTo(메이지대학_지원_정보.getKoreanName()),
                () -> assertThat(response.englishName()).isEqualTo(아시아_일본_메이지대학.getEnglishName()),
                () -> assertThat(response.region()).isEqualTo(아시아_일본_메이지대학.getRegion().getKoreanName()),
                () -> assertThat(response.country()).isEqualTo(아시아_일본_메이지대학.getCountry().getKoreanName()),
                () -> assertThat(response.languageRequirements()).isEqualTo(
                        메이지대학_지원_정보.getLanguageRequirements().stream()
                                .map(LanguageRequirementResponse::from)
                                .toList()),
                () -> assertThat(response.term()).isEqualTo(메이지대학_지원_정보.getTerm()),
                () -> assertThat(response.formatName()).isEqualTo(아시아_일본_메이지대학.getFormatName()),
                () -> assertThat(response.homepageUrl()).isEqualTo(아시아_일본_메이지대학.getHomepageUrl()),
                () -> assertThat(response.logoImageUrl()).isEqualTo(아시아_일본_메이지대학.getLogoImageUrl()),
                () -> assertThat(response.backgroundImageUrl()).isEqualTo(아시아_일본_메이지대학.getBackgroundImageUrl()),
                () -> assertThat(response.detailsForLocal()).isEqualTo(아시아_일본_메이지대학.getDetailsForLocal()),
                () -> assertThat(response.studentCapacity()).isEqualTo(메이지대학_지원_정보.getStudentCapacity()),
                () -> assertThat(response.tuitionFeeType()).isEqualTo(메이지대학_지원_정보.getTuitionFeeType().getKoreanName()),
                () -> assertThat(response.semesterAvailableForDispatch()).isEqualTo(메이지대학_지원_정보.getSemesterAvailableForDispatch().getKoreanName()),
                () -> assertThat(response.detailsForLanguage()).isEqualTo(메이지대학_지원_정보.getDetailsForLanguage()),
                () -> assertThat(response.gpaRequirement()).isEqualTo(메이지대학_지원_정보.getGpaRequirement()),
                () -> assertThat(response.gpaRequirementCriteria()).isEqualTo(메이지대학_지원_정보.getGpaRequirementCriteria()),
                () -> assertThat(response.semesterRequirement()).isEqualTo(메이지대학_지원_정보.getSemesterRequirement()),
                () -> assertThat(response.detailsForApply()).isEqualTo(메이지대학_지원_정보.getDetailsForApply()),
                () -> assertThat(response.detailsForMajor()).isEqualTo(메이지대학_지원_정보.getDetailsForMajor()),
                () -> assertThat(response.detailsForAccommodation()).isEqualTo(메이지대학_지원_정보.getDetailsForAccommodation()),
                () -> assertThat(response.detailsForEnglishCourse()).isEqualTo(메이지대학_지원_정보.getDetailsForEnglishCourse()),
                () -> assertThat(response.details()).isEqualTo(메이지대학_지원_정보.getDetails()),
                () -> assertThat(response.accommodationUrl()).isEqualTo(아시아_일본_메이지대학.getAccommodationUrl()),
                () -> assertThat(response.englishCourseUrl()).isEqualTo(아시아_일본_메이지대학.getEnglishCourseUrl())
        );
    }
}
