package com.example.solidconnection.e2e;

import com.example.solidconnection.config.token.TokenService;
import com.example.solidconnection.config.token.TokenType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.MyPageResponse;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static com.example.solidconnection.e2e.DynamicFixture.createSiteUserByEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("마이페이지 테스트")
class MyPageTest extends BaseEndToEndTest {

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private TokenService tokenService;

    private final String email = "email@email.com";
    private String accessToken;

    @BeforeEach
    public void setUpUserAndToken() {
        // setUp - 회원 정보 저장
        siteUserRepository.save(createSiteUserByEmail(email));

        // setUp - 엑세스 토큰 생성과 리프레시 토큰 생성 및 저장
        accessToken = tokenService.generateToken(email, TokenType.ACCESS);
        String refreshToken = tokenService.generateToken(email, TokenType.REFRESH);
        tokenService.saveToken(refreshToken, TokenType.REFRESH);
    }

    @Test
    void 마이페이지_정보를_조회한다() {
        // request - 요청
        MyPageResponse myPageResponse = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .log().all()
                .get("/my-page")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(MyPageResponse.class);

        SiteUser savedSiteUser = siteUserRepository.getByEmail(email);
        assertAll("불러온 마이 페이지 정보가 DB의 정보와 일치한다.",
                () -> assertThat(myPageResponse.nickname()).isEqualTo(savedSiteUser.getNickname()),
                () -> assertThat(myPageResponse.birth()).isEqualTo(savedSiteUser.getBirth()),
                () -> assertThat(myPageResponse.profileImageUrl()).isEqualTo(savedSiteUser.getProfileImageUrl()));
    }
}
