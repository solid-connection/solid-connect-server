package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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
        AccessToken accessToken = authTokenProvider.generateAccessToken(subject);

        // then
        String actualSubject = authTokenProvider.parseSubject(accessToken.token()).value();
        assertThat(actualSubject).isEqualTo(subject.value());
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
            AccessToken fakeRefreshToken = authTokenProvider.generateAccessToken(subject); // todo: issue#296

            // when, then
            assertAll(
                    () -> assertThat(authTokenProvider.isValidRefreshToken(refreshToken.token())).isTrue(),
                    () -> assertThat(authTokenProvider.isValidRefreshToken(fakeRefreshToken.token())).isFalse()
            );
        }

        @Test
        void 액세서_토큰에_해당하는_리프레시_토큰을_삭제한다() {
            // given
            authTokenProvider.generateAndSaveRefreshToken(subject);
            AccessToken accessToken = authTokenProvider.generateAccessToken(subject);

            // when
            authTokenProvider.deleteRefreshTokenByAccessToken(accessToken);

            // then
            String refreshTokenKey = TokenType.REFRESH.addPrefix(subject.value());
            assertThat(redisTemplate.opsForValue().get(refreshTokenKey)).isNull();
        }
    }

    @Nested
    class 블랙리스트를_관리한다 {

        @Test
        void 액세스_토큰을_블랙리스트에_추가한다() {
            // given
            AccessToken accessToken = authTokenProvider.generateAccessToken(subject); // todo: issue#296

            // when
            authTokenProvider.addToBlacklist(accessToken);

            // then
            String blackListTokenKey = TokenType.BLACKLIST.addPrefix(accessToken.token());
            String foundBlackListToken = redisTemplate.opsForValue().get(blackListTokenKey);
            assertThat(foundBlackListToken).isNotNull();
        }

        /*
        * todo: JwtUtils 나 TokenProvider 를 스프링 빈으로 주입받도록 변경한다. (issue#296)
        *  - 아래 테스트 코드에서는, 내부적으로 JwtUtils.parseSubject() 메서드가 호출될 때 발생하는 예외를 피하기 위해 jwt토큰을 생성한다.
        *  - 테스트 작성자는 예외 발생을 피하기 위해 "제대로된 jwt 토큰 생성이 필요하다"는 것을 몰라야한다.
        *  - 따라서, JwtUtils 나 TokenProvider 를 스프링 빈으로 주입받도록 변경하고, 테스트에서 mock 을 사용하여 의존성을 끊을 필요가 있다.
         */
        @Test
        void 블랙리스트에_있는_토큰인지_확인한다() {
            // given
            AccessToken accessToken = authTokenProvider.generateAccessToken(subject);
            authTokenProvider.addToBlacklist(accessToken);
            AccessToken notRegisteredAccessToken = authTokenProvider.generateAccessToken(new Subject("!"));

            // when, then
            assertAll(
                    () -> assertThat(authTokenProvider.isTokenBlacklisted(accessToken.token())).isTrue(),
                    () -> assertThat(authTokenProvider.isTokenBlacklisted(notRegisteredAccessToken.token())).isFalse()
            );
        }
    }

    @Test
    void 토큰으로부터_Subject_를_추출한다() {
        // given
        String accessToken = authTokenProvider.generateAccessToken(subject).token();

        // when
        Subject actualSubject = authTokenProvider.parseSubject(accessToken);

        // then
        assertThat(actualSubject.value()).isEqualTo(subject.value());
    }
}
