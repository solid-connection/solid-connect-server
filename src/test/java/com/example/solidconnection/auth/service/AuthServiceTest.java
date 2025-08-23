package com.example.solidconnection.auth.service;

import static com.example.solidconnection.common.exception.ErrorCode.REFRESH_TOKEN_EXPIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.auth.domain.AccessToken;
import com.example.solidconnection.auth.domain.RefreshToken;
import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.auth.dto.ReissueResponse;
import com.example.solidconnection.auth.token.TokenBlackListService;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

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

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private TokenProvider tokenProvider;

    private SiteUser siteUser;
    private AccessToken accessToken;
    private String expectedSubject;

    @BeforeEach
    void setUp() {
        siteUser = siteUserFixture.사용자();
        accessToken = authTokenProvider.generateAccessToken(siteUser);
        expectedSubject = tokenProvider.parseSubject(accessToken.token());
    }

    @Test
    void 로그아웃한다() {
        // when
        authService.signOut(accessToken.token());

        // then
        String refreshTokenKey = TokenType.REFRESH.addPrefix(expectedSubject);
        assertAll(
                () -> assertThat(redisTemplate.opsForValue().get(refreshTokenKey)).isNull(),
                () -> assertThat(tokenBlackListService.isTokenBlacklisted(accessToken.token())).isTrue()
        );
    }

    @Test
    void 탈퇴한다() {
        // when
        authService.quit(siteUser.getId(), accessToken.token());

        // then
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String refreshTokenKey = TokenType.REFRESH.addPrefix(expectedSubject);
        SiteUser actualSitUser = siteUserRepository.findById(siteUser.getId()).orElseThrow();
        assertAll(
                () -> assertThat(actualSitUser.getQuitedAt()).isEqualTo(tomorrow),
                () -> assertThat(redisTemplate.opsForValue().get(refreshTokenKey)).isNull(),
                () -> assertThat(tokenBlackListService.isTokenBlacklisted(accessToken.token())).isTrue()
        );
    }

    @Nested
    class 토큰을_재발급한다 {

        @Test
        void 요청의_리프레시_토큰이_저장되어_있으면_액세스_토큰을_재발급한다() {
            // given
            RefreshToken refreshToken = authTokenProvider.generateAndSaveRefreshToken(siteUser);

            // when
            ReissueResponse reissuedAccessToken = authService.reissue(refreshToken.token());

            // then - 요청의 리프레시 토큰과 재발급한 액세스 토큰의 주체가 동일해야 한다.
            SiteUser actualSiteUser = authTokenProvider.parseSiteUser(refreshToken.token());
            SiteUser expectedSiteUser = authTokenProvider.parseSiteUser(reissuedAccessToken.accessToken());
            assertThat(actualSiteUser.getId()).isEqualTo(expectedSiteUser.getId());
        }

        @Test
        void 요청의_리프레시_토큰이_저장되어있지_않다면_예외가_발생한다() {
            // given
            String invalidRefreshToken = accessToken.token();

            // when, then
            assertThatCode(() -> authService.reissue(invalidRefreshToken))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(REFRESH_TOKEN_EXPIRED.getMessage());
        }
    }
}
