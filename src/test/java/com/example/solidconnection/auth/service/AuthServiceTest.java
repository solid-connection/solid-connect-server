package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.dto.ReissueRequest;
import com.example.solidconnection.auth.dto.ReissueResponse;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
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

    @Test
    void 로그아웃한다() {
        // given
        AccessToken accessToken = authTokenProvider.generateAccessToken(new Subject("subject")); // todo: #296

        // when
        authService.signOut(accessToken);

        // then
        assertThat(authTokenProvider.isTokenBlacklisted(accessToken.token())).isTrue();
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
