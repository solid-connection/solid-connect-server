package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.auth.dto.ReissueRequest;
import com.example.solidconnection.auth.dto.ReissueResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;

import static com.example.solidconnection.common.exception.ErrorCode.REFRESH_TOKEN_EXPIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("인증 서비스 테스트")
@TestContainerSpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthTokenProvider authTokenProvider;

    @Autowired
    private TokenBlackListService tokenBlackListService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Test
    void 로그아웃한다() {
        // given
        Subject subject = new Subject("subject");
        AccessToken accessToken = authTokenProvider.generateAccessToken(subject);

        // when
        authService.signOut(accessToken.token());

        // then
        String refreshTokenKey = TokenType.REFRESH.addPrefix(subject.value());
        assertAll(
                () -> assertThat(redisTemplate.opsForValue().get(refreshTokenKey)).isNull(),
                () -> assertThat(tokenBlackListService.isTokenBlacklisted(accessToken.token())).isTrue()
        );
    }

    @Test
    void 탈퇴한다() {
        // given
        SiteUser user = siteUserFixture.사용자();
        Subject subject = authTokenProvider.toSubject(user);
        AccessToken accessToken = authTokenProvider.generateAccessToken(subject);

        // when
        authService.quit(user, accessToken.token());

        // then
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String refreshTokenKey = TokenType.REFRESH.addPrefix(subject.value());
        assertAll(
                () -> assertThat(user.getQuitedAt()).isEqualTo(tomorrow),
                () -> assertThat(redisTemplate.opsForValue().get(refreshTokenKey)).isNull(),
                () -> assertThat(tokenBlackListService.isTokenBlacklisted(accessToken.token())).isTrue()
        );
    }

    @Nested
    class 토큰을_재발급한다 {

        @Test
        void 요청의_리프레시_토큰이_저장되어_있으면_액세스_토큰을_재발급한다() {
            // given
            RefreshToken refreshToken = authTokenProvider.generateAndSaveRefreshToken(new Subject("subject"));
            ReissueRequest reissueRequest = new ReissueRequest(refreshToken.token());

            // when
            ReissueResponse reissuedAccessToken = authService.reissue(reissueRequest);

            // then - 요청의 리프레시 토큰과 재발급한 액세스 토큰의 subject 가 동일해야 한다.
            Subject expectedSubject = authTokenProvider.parseSubject(refreshToken.token());
            Subject actualSubject = authTokenProvider.parseSubject(reissuedAccessToken.accessToken());
            assertThat(actualSubject).isEqualTo(expectedSubject);
        }

        @Test
        void 요청의_리프레시_토큰이_저장되어있지_않다면_예외_응답을_반환한다() {
            // given
            String invalidRefreshToken = authTokenProvider.generateAccessToken(new Subject("subject")).token();
            ReissueRequest reissueRequest = new ReissueRequest(invalidRefreshToken);

            // when, then
            assertThatCode(() -> authService.reissue(reissueRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(REFRESH_TOKEN_EXPIRED.getMessage());
        }
    }
}
