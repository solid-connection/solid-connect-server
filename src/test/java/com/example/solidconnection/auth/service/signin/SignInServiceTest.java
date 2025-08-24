package com.example.solidconnection.auth.service.signin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.auth.domain.RefreshToken;
import com.example.solidconnection.auth.domain.Subject;
import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.auth.service.TokenProvider;
import com.example.solidconnection.auth.service.TokenStorage;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("로그인 서비스 테스트")
@TestContainerSpringBootTest
class SignInServiceTest {

    @Autowired
    private SignInService signInService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private TokenStorage tokenStorage;

    @Autowired
    private SiteUserFixture siteUserFixture;

    private SiteUser user;
    private Subject subject;

    @BeforeEach
    void setUp() {
        user = siteUserFixture.사용자();
        subject = new Subject(user.getId().toString());
    }

    @Test
    void 성공적으로_로그인한다() {
        // when
        SignInResponse signInResponse = signInService.signIn(user);

        // then
        Subject accessTokenSubject = tokenProvider.parseSubject(signInResponse.accessToken());
        Subject refreshTokenSubject = tokenProvider.parseSubject(signInResponse.refreshToken());
        Optional<String> savedRefreshToken = tokenStorage.findToken(subject, RefreshToken.class);
        assertAll(
                () -> assertThat(accessTokenSubject).isEqualTo(subject),
                () -> assertThat(refreshTokenSubject).isEqualTo(subject),
                () -> assertThat(savedRefreshToken).hasValue(signInResponse.refreshToken()));
    }

    @Test
    void 탈퇴한_이력이_있으면_초기화한다() {
        // given
        user.setQuitedAt(LocalDate.now().minusDays(1));

        // when
        signInService.signIn(user);

        // then
        assertThat(user.getQuitedAt()).isNull();
    }
}
