package com.example.solidconnection.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.auth.domain.AccessToken;
import com.example.solidconnection.auth.domain.RefreshToken;
import com.example.solidconnection.auth.domain.Subject;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("인증 토큰 제공자 테스트")
class AuthTokenProviderTest {

    @Autowired
    private AuthTokenProvider authTokenProvider;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private TokenStorage tokenStorage;

    @Autowired
    private SiteUserFixture siteUserFixture;

    private SiteUser siteUser;
    private Subject expectedSubject;

    @BeforeEach
    void setUp() {
        siteUser = siteUserFixture.사용자();
        expectedSubject = new Subject(siteUser.getId().toString());
    }

    @Test
    void 액세스_토큰을_생성한다() {
        // when
        AccessToken accessToken = authTokenProvider.generateAccessToken(siteUser);

        // then
        String accessTokenValue = accessToken.token();
        Subject actualSubject = tokenProvider.parseSubject(accessTokenValue);
        Role actualRole = authTokenProvider.parseSiteUser(accessTokenValue).getRole();
        assertAll(
                () -> assertThat(accessTokenValue).isNotNull(),
                () -> assertThat(actualSubject).isEqualTo(expectedSubject),
                () -> assertThat(actualRole).isEqualTo(siteUser.getRole())
        );
    }

    @Nested
    class 리프레시_토큰을_제공한다 {

        @Test
        void 리프레시_토큰을_생성하고_저장한다() {
            // when
            RefreshToken refreshToken = authTokenProvider.generateAndSaveRefreshToken(siteUser);

            // then
            Subject actualSubject = tokenProvider.parseSubject(refreshToken.token());
            Optional<String> savedRefreshToken = tokenStorage.findToken(expectedSubject, RefreshToken.class);
            assertAll(
                    () -> assertThat(savedRefreshToken).hasValue(refreshToken.token()),
                    () -> assertThat(actualSubject).isEqualTo(expectedSubject)
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
            assertThat(tokenStorage.findToken(expectedSubject, RefreshToken.class)).isEmpty();
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
