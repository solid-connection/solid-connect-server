package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.auth.dto.ReissueRequest;
import com.example.solidconnection.auth.dto.ReissueResponse;
import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import com.example.solidconnection.util.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static com.example.solidconnection.custom.exception.ErrorCode.REFRESH_TOKEN_EXPIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("인증 서비스 테스트")
@TestContainerSpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthTokenProvider authTokenProvider;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void 로그아웃한다() {
        // given
        String accessToken = "accessToken";

        // when
        authService.signOut(accessToken);

        // then
        assertThat(authTokenProvider.findBlackListToken(accessToken)).isNotNull();
    }

    @Test
    void 탈퇴한다() {
        // given
        SiteUser siteUser = createSiteUser();

        // when
        authService.quit(siteUser);

        // then
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        assertThat(siteUser.getQuitedAt()).isEqualTo(tomorrow);
    }

    @Nested
    class 토큰을_재발급한다 {

        @Test
        void 요청의_리프레시_토큰이_저장되어_있고_값이_일치면_액세스_토큰을_재발급한다() {
            // given
            SiteUser siteUser = createSiteUser();
            String refreshToken = authTokenProvider.generateAndSaveRefreshToken(siteUser);
            ReissueRequest reissueRequest = new ReissueRequest(refreshToken);

            // when
            ReissueResponse reissuedAccessToken = authService.reissue(reissueRequest);

            // then
            String actualSubject = JwtUtils.parseSubject(reissuedAccessToken.accessToken(), jwtProperties.secret());
            String expectedSubject = JwtUtils.parseSubject(refreshToken, jwtProperties.secret());
            assertThat(actualSubject).isEqualTo(expectedSubject);
        }

        @Test
        void 요청의_리프레시_토큰이_저장되어있지_않다면_예외_응답을_반환한다() {
            // given
            String refreshToken = authTokenProvider.generateToken("subject", TokenType.REFRESH);
            ReissueRequest reissueRequest = new ReissueRequest(refreshToken);

            // when, then
            assertThatCode(() -> authService.reissue(reissueRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(REFRESH_TOKEN_EXPIRED.getMessage());
        }
    }

    private SiteUser createSiteUser() {
        SiteUser siteUser = new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                PreparationStatus.CONSIDERING,
                Role.MENTEE
        );
        return siteUserRepository.save(siteUser);
    }
}
