package com.example.solidconnection.e2e;

import com.example.solidconnection.auth.client.KakaoOAuthClient;
import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.auth.dto.kakao.FirstAccessResponse;
import com.example.solidconnection.auth.dto.kakao.KakaoCodeRequest;
import com.example.solidconnection.auth.dto.kakao.KakaoUserInfoDto;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

import static com.example.solidconnection.auth.domain.TokenType.KAKAO_OAUTH;
import static com.example.solidconnection.auth.domain.TokenType.REFRESH;
import static com.example.solidconnection.e2e.DynamicFixture.createKakaoUserInfoDtoByEmail;
import static com.example.solidconnection.e2e.DynamicFixture.createSiteUserByEmail;
import static com.example.solidconnection.scheduler.UserRemovalScheduler.ACCOUNT_RECOVER_DURATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

@DisplayName("카카오 로그인 테스트")
class SignInTest extends BaseEndToEndTest {

    @Autowired
    SiteUserRepository siteUserRepository;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @MockBean
    KakaoOAuthClient kakaoOAuthClient;

    @Test
    void 신규_회원이_카카오로_로그인한다() {
        // stub - kakaoOAuthClient 가 정해진 사용자 프로필 정보를 반환하도록
        String kakaoCode = "kakaoCode";
        String email = "email@email.com";
        KakaoUserInfoDto kakaoUserInfoDto = createKakaoUserInfoDtoByEmail(email);
        given(kakaoOAuthClient.processOauth(kakaoCode))
                .willReturn(kakaoUserInfoDto);

        // request - body 생성 및 요청
        KakaoCodeRequest kakaoCodeRequest = new KakaoCodeRequest(kakaoCode);
        FirstAccessResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(kakaoCodeRequest)
                .when().post("/auth/kakao")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(FirstAccessResponse.class);

        KakaoUserInfoDto.KakaoAccountDto.KakaoProfileDto kakaoProfileDto = kakaoUserInfoDto.kakaoAccountDto().profile();
        assertAll("카카오톡 사용자 정보를 응답한다.",
                () -> assertThat(response.isRegistered()).isFalse(),
                () -> assertThat(response.email()).isEqualTo(email),
                () -> assertThat(response.nickname()).isEqualTo(kakaoProfileDto.nickname()),
                () -> assertThat(response.profileImageUrl()).isEqualTo(kakaoProfileDto.profileImageUrl()),
                () -> assertThat(response.kakaoOauthToken()).isNotNull());
        assertThat(redisTemplate.opsForValue().get(KAKAO_OAUTH.addPrefixToSubject(email)))
                .as("카카오 인증 토큰을 저장한다.")
                .isEqualTo(response.kakaoOauthToken());
    }

    @Test
    void 기존_회원이_카카오로_로그인한다() {
        // stub - kakaoOAuthClient 가 정해진 사용자 프로필 정보를 반환하도록
        String kakaoCode = "kakaoCode";
        String email = "email@email.com";
        given(kakaoOAuthClient.processOauth(kakaoCode))
                .willReturn(createKakaoUserInfoDtoByEmail(email));

        // setUp - 사용자 정보 저장
        SiteUser siteUser = siteUserRepository.save(createSiteUserByEmail(email));

        // request - body 생성 및 요청
        KakaoCodeRequest kakaoCodeRequest = new KakaoCodeRequest(kakaoCode);
        SignInResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(kakaoCodeRequest)
                .when().post("/auth/kakao")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(SignInResponse.class);

        assertAll("리프레스 토큰과 엑세스 토큰을 응답한다.",
                () -> assertThat(response.isRegistered()).isTrue(),
                () -> assertThat(response.accessToken()).isNotNull(),
                () -> assertThat(response.refreshToken()).isNotNull());
        assertThat(redisTemplate.opsForValue().get(REFRESH.addPrefixToSubject(siteUser.getId().toString())))
                .as("리프레시 토큰을 저장한다.")
                .isEqualTo(response.refreshToken());
    }

    @Test
    void 탈퇴한_회원이_계정_복구_기간_안에_다시_로그인하면_탈퇴가_무효화된다() {
        // stub - kakaoOAuthClient 가 정해진 사용자 프로필 정보를 반환하도록
        String kakaoCode = "kakaoCode";
        String email = "email@email.com";
        given(kakaoOAuthClient.processOauth(kakaoCode))
                .willReturn(createKakaoUserInfoDtoByEmail(email));

        // setUp - 계정 복구 기간이 되지 않은 사용자 저장
        SiteUser siteUserFixture = createSiteUserByEmail(email);
        LocalDate justBeforeRemoval = LocalDate.now().minusDays(ACCOUNT_RECOVER_DURATION - 1);
        siteUserFixture.setQuitedAt(justBeforeRemoval);
        SiteUser siteUser = siteUserRepository.save(siteUserFixture);

        // request - body 생성 및 요청
        KakaoCodeRequest kakaoCodeRequest = new KakaoCodeRequest(kakaoCode);
        SignInResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(kakaoCodeRequest)
                .when().post("/auth/kakao")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(SignInResponse.class);

        SiteUser updatedSiteUser = siteUserRepository.findById(siteUser.getId()).get();
        assertAll("리프레스 토큰과 엑세스 토큰을 응답하고, 탈퇴 날짜를 초기화한다.",
                () -> assertThat(response.isRegistered()).isTrue(),
                () -> assertThat(response.accessToken()).isNotNull(),
                () -> assertThat(response.refreshToken()).isNotNull(),
                () -> assertThat(updatedSiteUser.getQuitedAt()).isNull());
        assertThat(redisTemplate.opsForValue().get(REFRESH.addPrefixToSubject(siteUser.getId().toString())))
                .as("리프레시 토큰을 저장한다.")
                .isEqualTo(response.refreshToken());
    }
}
