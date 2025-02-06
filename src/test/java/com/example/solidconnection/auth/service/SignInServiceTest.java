package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import com.example.solidconnection.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("로그인 서비스 테스트")
@TestContainerSpringBootTest
class SignInServiceTest {

    @Autowired
    private SignInService signInService;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private AuthTokenProvider authTokenProvider;

    @Autowired
    private SiteUserRepository siteUserRepository;

    private SiteUser siteUser;
    private String subject;

    @BeforeEach
    void setUp() {
        siteUser = createSiteUser();
        siteUserRepository.save(siteUser);
        subject = siteUser.getId().toString();
    }

    @Test
    void 성공적으로_로그인한다() {
        // when
        SignInResponse signInResponse = signInService.signIn(siteUser);

        // then
        String accessTokenSubject = JwtUtils.parseSubject(signInResponse.accessToken(), jwtProperties.secret());
        String refreshTokenSubject = JwtUtils.parseSubject(signInResponse.refreshToken(), jwtProperties.secret());
        Optional<String> savedRefreshToken = authTokenProvider.findRefreshToken(subject);
        assertAll(
                () -> assertThat(accessTokenSubject).isEqualTo(subject),
                () -> assertThat(refreshTokenSubject).isEqualTo(subject),
                () -> assertThat(savedRefreshToken).hasValue(signInResponse.refreshToken()));
    }

    @Test
    void 탈퇴한_이력이_있으면_초기화한다() {
        // given
        siteUser.setQuitedAt(LocalDate.now().minusDays(1));
        siteUserRepository.save(siteUser);

        // when
        signInService.signIn(siteUser);

        // then
        assertThat(siteUser.getQuitedAt()).isNull();
    }

    private SiteUser createSiteUser() {
        return new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
    }
}
