package com.example.solidconnection.e2e;

import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.auth.dto.kakao.FirstAccessResponse;
import com.example.solidconnection.auth.dto.kakao.KakaoCodeRequest;
import com.example.solidconnection.auth.dto.kakao.KakaoUserInfoDto;
import com.example.solidconnection.auth.service.KakaoOAuthClient;
import com.example.solidconnection.auth.service.SignInService;
import com.example.solidconnection.custom.response.DataResponse;
import com.example.solidconnection.entity.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

import static com.example.solidconnection.scheduler.UserRemovalScheduler.ACCOUNT_RECOVER_DURATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

class SignInTest extends BaseEndToEndTest {

    @Autowired
    SignInService signInService;

    @Autowired
    SiteUserRepository siteUserRepository;

    @MockBean
    KakaoOAuthClient kakaoOAuthClient;

    private SiteUser createSiteUserFixture(String email) {
        return new SiteUser(
                email,
                "nickname",
                "profileImage",
                "2000-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.FEMALE,
                null,
                null
        );
    }

    private KakaoUserInfoDto createKakaoUserInfoDto(String email) {
        return new KakaoUserInfoDto(
                new KakaoUserInfoDto.KakaoAccountDto(
                        new KakaoUserInfoDto.KakaoAccountDto.KakaoProfileDto(
                                "nickname",
                                "profileImageUrl"
                        ),
                        email
                )
        );
    }

    @Test
    void 신규_회원이_카카오로_로그인한다() {
        String kakaoCode = "kakaoCode";
        String email = "email@email.com";
        given(kakaoOAuthClient.processOauth(kakaoCode))
                .willReturn(createKakaoUserInfoDto(email));

        KakaoCodeRequest kakaoCodeRequest = new KakaoCodeRequest(kakaoCode);
        FirstAccessResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(kakaoCodeRequest)
                .when().post("/auth/kakao")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<DataResponse<FirstAccessResponse>>() {})
                .getData();

        assertAll(
                "카카오톡 사용자 정보를 응답한다.",
                () -> assertThat(response.isRegistered()).isFalse(),
                () -> assertThat(response.email()).isEqualTo(email),
                () -> assertThat(response.nickname()).isNotNull(),
                () -> assertThat(response.profileImageUrl()).isNotNull(),
                () -> assertThat(response.kakaoOauthToken()).isNotNull()
        );
    }

    @Test
    void 기존_회원이_카카오로_로그인한다() {
        String kakaoCode = "kakaoCode";
        String email = "email@email.com";
        siteUserRepository.save(createSiteUserFixture(email));
        given(kakaoOAuthClient.processOauth(kakaoCode))
                .willReturn(createKakaoUserInfoDto(email));

        KakaoCodeRequest kakaoCodeRequest = new KakaoCodeRequest(kakaoCode);
        SignInResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(kakaoCodeRequest)
                .when().post("/auth/kakao")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<DataResponse<SignInResponse>>() {})
                .getData();

        assertAll(
                "리프레스 토큰과 엑세스 토큰을 응답한다.",
                () -> assertThat(response.isRegistered()).isTrue(),
                () -> assertThat(response.accessToken()).isNotNull(),
                () -> assertThat(response.refreshToken()).isNotNull()
        );
    }

    @Test
    void 탈퇴한_회원이_계정_복구_기간_안에_다시_로그인하면_탈퇴가_무효화된다() {
        String kakaoCode = "kakaoCode";
        String email = "email@email.com";
        SiteUser siteUserFixture = createSiteUserFixture(email);
        LocalDate justBeforeRemoval = LocalDate.now().minusDays(ACCOUNT_RECOVER_DURATION - 1);
        siteUserFixture.setQuitedAt(justBeforeRemoval);
        siteUserRepository.save(siteUserFixture);
        given(kakaoOAuthClient.processOauth(kakaoCode))
                .willReturn(createKakaoUserInfoDto(email));

        KakaoCodeRequest kakaoCodeRequest = new KakaoCodeRequest(kakaoCode);
        SignInResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(kakaoCodeRequest)
                .when().post("/auth/kakao")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<DataResponse<SignInResponse>>() {})
                .getData();

        assertAll(
                "리프레스 토큰와 엑세스 토큰을 응답하고, 탈퇴 날짜를 초기화한다.",
                () -> assertThat(response.isRegistered()).isTrue(),
                () -> assertThat(response.accessToken()).isNotNull(),
                () -> assertThat(response.refreshToken()).isNotNull(),
                () -> assertThat(siteUserRepository.getByEmail(email).getQuitedAt()).isNull()
        );
    }
}
