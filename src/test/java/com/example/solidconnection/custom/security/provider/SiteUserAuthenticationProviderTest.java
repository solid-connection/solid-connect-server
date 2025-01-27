package com.example.solidconnection.custom.security.provider;

import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.security.authentication.SiteUserAuthentication;
import com.example.solidconnection.custom.security.userdetails.SiteUserDetails;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import java.net.PasswordAuthentication;
import java.util.Date;

import static com.example.solidconnection.custom.exception.ErrorCode.AUTHENTICATION_FAILED;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestContainerSpringBootTest
@DisplayName("사용자 인증정보 provider 테스트")
class SiteUserAuthenticationProviderTest {

    @Autowired
    private SiteUserAuthenticationProvider siteUserAuthenticationProvider;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private SiteUserRepository siteUserRepository;

    private SiteUser siteUser;

    @BeforeEach
    void setUp() {
        siteUser = createSiteUser();
        siteUserRepository.save(siteUser);
    }

    @Test
    void 처리할_수_있는_타입인지를_반환한다() {
        // given
        Class<?> supportedType = SiteUserAuthentication.class;
        Class<?> notSupportedType = PasswordAuthentication.class;

        // when & then
        assertAll(
                () -> assertThat(siteUserAuthenticationProvider.supports(supportedType)).isTrue(),
                () -> assertThat(siteUserAuthenticationProvider.supports(notSupportedType)).isFalse()
        );
    }

    @Test
    void 유효한_토큰이면_정상적으로_인증_정보를_반환한다() {
        // given
        String token = createValidToken(siteUser.getId());
        SiteUserAuthentication auth = new SiteUserAuthentication(token);

        // when
        Authentication result = siteUserAuthenticationProvider.authenticate(auth);

        // then
        assertThat(result).isNotNull();
        assertAll(
                () -> assertThat(result.getCredentials()).isEqualTo(token),
                () -> assertThat(result.getPrincipal().getClass()).isEqualTo(SiteUserDetails.class)
        );
    }

    @Nested
    class 예외_응답을_반환하다 {

        @Test
        void 유효하지_않은_토큰이면_예외_응답을_반환한다() {
            // given
            SiteUserAuthentication expiredAuth = new SiteUserAuthentication(createExpiredToken());

            // when & then
            assertThatCode(() -> siteUserAuthenticationProvider.authenticate(expiredAuth))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(INVALID_TOKEN.getMessage());
        }

        @Test
        void 사용자_정보의_형식이_다르면_예외_응답을_반환한다() {
            // given
            SiteUserAuthentication wrongSubjectTypeAuth = new SiteUserAuthentication(createWrongSubjectTypeToken());

            // when & then
            assertThatCode(() -> siteUserAuthenticationProvider.authenticate(wrongSubjectTypeAuth))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(INVALID_TOKEN.getMessage());
        }

        @Test
        void 유효한_토큰이지만_해당되는_사용자가_없으면_예외_응답을_반환한다() {
            // given
            long notExistingUserId = siteUser.getId() + 100;
            String token = createValidToken(notExistingUserId);
            SiteUserAuthentication auth = new SiteUserAuthentication(token);

            // when & then
            assertThatCode(() -> siteUserAuthenticationProvider.authenticate(auth))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(AUTHENTICATION_FAILED.getMessage());
        }
    }

    private String createValidToken(long id) {
        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.secret())
                .compact();
    }

    private String createExpiredToken() {
        return Jwts.builder()
                .setSubject(String.valueOf(siteUser.getId()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.secret())
                .compact();
    }

    private String createWrongSubjectTypeToken() {
        return Jwts.builder()
                .setSubject("subject")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.secret())
                .compact();
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
