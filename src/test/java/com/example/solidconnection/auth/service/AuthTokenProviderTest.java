package com.example.solidconnection.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

@TestContainerSpringBootTest
@DisplayName("인증 토큰 제공자 테스트")
class AuthTokenProviderTest {

    @Autowired
    private AuthTokenProvider authTokenProvider;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private SiteUserFixture siteUserFixture;

    private SiteUser siteUser;
    private String expectedSubject;

    @BeforeEach
    void setUp() {
        siteUser = siteUserFixture.사용자();
        expectedSubject = siteUser.getId().toString();
    }

    @Test
    void 액세스_토큰을_생성한다() {
        // when
        AccessToken accessToken = authTokenProvider.generateAccessToken(siteUser);

        // then
        assertAll(
                () -> assertThat(accessToken.subject().value()).isEqualTo(expectedSubject),
                () -> assertThat(accessToken.role()).isEqualTo(siteUser.getRole()),
                () -> assertThat(accessToken.token()).isNotNull()
        );
    }

    @Nested
    class 리프레시_토큰을_제공한다 {

        @Test
        void 리프레시_토큰을_생성하고_저장한다() {
            // when
            RefreshToken actualRefreshToken = authTokenProvider.generateAndSaveRefreshToken(siteUser);

            // then
            String refreshTokenKey = TokenType.REFRESH.addPrefix(expectedSubject);
            String expectedRefreshToken = redisTemplate.opsForValue().get(refreshTokenKey);
            assertAll(
                    () -> assertThat(actualRefreshToken.subject().value()).isEqualTo(expectedSubject),
                    () -> assertThat(actualRefreshToken.token()).isEqualTo(expectedRefreshToken)
            );
        }

        @Test
        void 유효한_리프레시_토큰인지_확인한다() {
            // given
            RefreshToken refreshToken = authTokenProvider.generateAndSaveRefreshToken(siteUser);
            AccessToken fakeRefreshToken = authTokenProvider.generateAccessToken(siteUser);

            // when, then
            assertAll(
                    () -> assertThat(authTokenProvider.isValidRefreshToken(refreshToken.token())).isTrue(),
                    () -> assertThat(authTokenProvider.isValidRefreshToken(fakeRefreshToken.token())).isFalse()
            );
        }

        @Test
        void 액세스_토큰에_해당하는_리프레시_토큰을_삭제한다() {
            // given
            authTokenProvider.generateAndSaveRefreshToken(siteUser);
            AccessToken accessToken = authTokenProvider.generateAccessToken(siteUser);

            // when
            authTokenProvider.deleteRefreshTokenByAccessToken(accessToken);

            // then
            String refreshTokenKey = TokenType.REFRESH.addPrefix(expectedSubject);
            assertThat(redisTemplate.opsForValue().get(refreshTokenKey)).isNull();
        }
    }

    @Test
    void 토큰으로부터_SiteUser_를_추출한다() {
        // given
        String accessToken = authTokenProvider.generateAccessToken(siteUser).token();

        // when
        SiteUser actualSitUser = authTokenProvider.parseSiteUser(accessToken);

        // then
        assertThat(actualSitUser.getId()).isEqualTo(siteUser.getId());
    }
}
