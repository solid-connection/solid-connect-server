package com.example.solidconnection.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.siteuser.domain.Role;
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

    private Subject subject;

    @BeforeEach
    void setUp() {
        subject = new Subject("subject123");
    }

    @Test
    void 액세스_토큰을_생성한다() {
        // when
        Role expectedRole = Role.MENTEE;
        AccessToken accessToken = authTokenProvider.generateAccessToken(subject, expectedRole);

        // then
        String actualSubject = authTokenProvider.parseSubject(accessToken.token()).value();
        assertAll(
                () -> assertThat(actualSubject).isEqualTo(subject.value()),
                () -> assertThat(accessToken.role()).isEqualTo(expectedRole),
                () -> assertThat(accessToken.token()).isNotNull()
        );
    }

    @Nested
    class 리프레시_토큰을_제공한다 {

        @Test
        void 리프레시_토큰을_생성하고_저장한다() {
            // when
            RefreshToken actualRefreshToken = authTokenProvider.generateAndSaveRefreshToken(subject);

            // then
            String actualSubject = authTokenProvider.parseSubject(actualRefreshToken.token()).value();
            String refreshTokenKey = TokenType.REFRESH.addPrefix(subject.value());
            String expectedRefreshToken = redisTemplate.opsForValue().get(refreshTokenKey);
            assertAll(
                    () -> assertThat(actualSubject).isEqualTo(subject.value()),
                    () -> assertThat(actualRefreshToken.token()).isEqualTo(expectedRefreshToken)
            );
        }

        @Test
        void 유효한_리프레시_토큰인지_확인한다() {
            // given
            RefreshToken refreshToken = authTokenProvider.generateAndSaveRefreshToken(subject);
            AccessToken fakeRefreshToken = authTokenProvider.generateAccessToken(subject, Role.MENTEE);

            // when, then
            assertAll(
                    () -> assertThat(authTokenProvider.isValidRefreshToken(refreshToken.token())).isTrue(),
                    () -> assertThat(authTokenProvider.isValidRefreshToken(fakeRefreshToken.token())).isFalse()
            );
        }

        @Test
        void 액세스_토큰에_해당하는_리프레시_토큰을_삭제한다() {
            // given
            authTokenProvider.generateAndSaveRefreshToken(subject);
            AccessToken accessToken = authTokenProvider.generateAccessToken(subject, Role.MENTEE);

            // when
            authTokenProvider.deleteRefreshTokenByAccessToken(accessToken);

            // then
            String refreshTokenKey = TokenType.REFRESH.addPrefix(subject.value());
            assertThat(redisTemplate.opsForValue().get(refreshTokenKey)).isNull();
        }
    }

    @Test
    void 토큰으로부터_Subject_를_추출한다() {
        // given
        String accessToken = authTokenProvider.generateAccessToken(subject, Role.MENTEE).token();

        // when
        Subject actualSubject = authTokenProvider.parseSubject(accessToken);

        // then
        assertThat(actualSubject.value()).isEqualTo(subject.value());
    }
}
