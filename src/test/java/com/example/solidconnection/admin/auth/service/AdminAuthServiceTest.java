package com.example.solidconnection.admin.auth.service;

import static com.example.solidconnection.common.exception.ErrorCode.ADMIN_REFRESH_TOKEN_EXPIRED;
import static com.example.solidconnection.common.exception.ErrorCode.NOT_ADMIN_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.admin.auth.dto.AdminReissueResponse;
import com.example.solidconnection.admin.auth.dto.AdminSignInRequest;
import com.example.solidconnection.admin.auth.dto.AdminSignInResult;
import com.example.solidconnection.auth.domain.AdminRefreshToken;
import com.example.solidconnection.auth.domain.Subject;
import com.example.solidconnection.auth.exception.AuthException;
import com.example.solidconnection.auth.service.AuthTokenProvider;
import com.example.solidconnection.auth.service.TokenProvider;
import com.example.solidconnection.auth.service.TokenStorage;
import com.example.solidconnection.auth.token.TokenBlackListService;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("어드민 인증 서비스 테스트")
@TestContainerSpringBootTest
class AdminAuthServiceTest {

    @Autowired
    private AdminAuthService adminAuthService;

    @Autowired
    private AuthTokenProvider authTokenProvider;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private TokenStorage tokenStorage;

    @Autowired
    private TokenBlackListService tokenBlackListService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    private SiteUser adminUser;
    private SiteUser regularUser;

    @BeforeEach
    void setUp() {
        adminUser = siteUserFixture.관리자();
        regularUser = siteUserFixture.사용자();
    }

    @Nested
    class 어드민_로그인 {

        @Test
        void 어드민_사용자가_로그인하면_어드민_리프레시_토큰이_저장된다() {
            // given
            AdminSignInRequest request = new AdminSignInRequest("admin@example.com", "admin123");

            // when
            AdminSignInResult result = adminAuthService.signIn(request);

            // then
            Subject subject = new Subject(adminUser.getId().toString());
            assertAll(
                    () -> assertThat(result.accessToken()).isNotNull(),
                    () -> assertThat(result.adminRefreshToken()).isNotNull(),
                    () -> assertThat(tokenStorage.findToken(subject, AdminRefreshToken.class))
                            .hasValue(result.adminRefreshToken())
            );
        }

        @Test
        void 어드민_권한이_없는_사용자가_로그인하면_예외가_발생한다() {
            // given
            AdminSignInRequest request = new AdminSignInRequest("test@example.com", "password123");

            // when & then
            assertThatCode(() -> adminAuthService.signIn(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(NOT_ADMIN_USER.getMessage());
        }
    }

    @Nested
    class 어드민_토큰_재발급 {

        @Test
        void 저장된_어드민_리프레시_토큰으로_액세스_토큰을_재발급한다() {
            // given
            AdminRefreshToken adminRefreshToken = authTokenProvider.generateAndSaveAdminRefreshToken(adminUser);

            // when
            AdminReissueResponse response = adminAuthService.reissue(adminRefreshToken.token());

            // then - 재발급된 액세스 토큰과 어드민 리프레시 토큰의 주체가 동일해야 한다
            SiteUser tokenSiteUser = authTokenProvider.parseSiteUser(adminRefreshToken.token());
            SiteUser reissuedSiteUser = authTokenProvider.parseSiteUser(response.accessToken());
            assertThat(tokenSiteUser.getId()).isEqualTo(reissuedSiteUser.getId());
        }

        @Test
        void 저장되지_않은_어드민_리프레시_토큰으로_재발급하면_예외가_발생한다() {
            // given
            AdminRefreshToken adminRefreshToken = authTokenProvider.generateAndSaveAdminRefreshToken(adminUser);
            tokenStorage.deleteToken(new Subject(adminUser.getId().toString()), AdminRefreshToken.class);

            // when & then
            assertThatCode(() -> adminAuthService.reissue(adminRefreshToken.token()))
                    .isInstanceOf(AuthException.class)
                    .hasMessage(ADMIN_REFRESH_TOKEN_EXPIRED.getMessage());
        }
    }

    @Nested
    class 어드민_로그아웃 {

        @Test
        void 로그아웃하면_어드민_리프레시_토큰이_삭제되고_액세스_토큰이_블랙리스트에_추가된다() {
            // given
            String accessToken = authTokenProvider.generateAccessToken(adminUser).token();
            authTokenProvider.generateAndSaveAdminRefreshToken(adminUser);
            Subject subject = new Subject(adminUser.getId().toString());

            // when
            adminAuthService.signOut(accessToken);

            // then
            assertAll(
                    () -> assertThat(tokenStorage.findToken(subject, AdminRefreshToken.class)).isEmpty(),
                    () -> assertThat(tokenBlackListService.isTokenBlacklisted(accessToken)).isTrue()
            );
        }
    }
}
