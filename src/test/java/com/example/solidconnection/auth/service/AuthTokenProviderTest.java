package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import com.example.solidconnection.util.JwtUtils;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestContainerSpringBootTest
@DisplayName("인증 토큰 제공자 테스트")
class AuthTokenProviderTest {

    @Autowired
    private AuthTokenProvider authTokenProvider;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtProperties jwtProperties;

    private SiteUser siteUser;
    private String subject;

    @BeforeEach
    void setUp() {
        siteUser = createSiteUser();
        siteUserRepository.save(siteUser);
        subject = siteUser.getId().toString();
    }

    @Nested
    class 액세스_토큰을_제공한다 {

        @Test
        void SiteUser_로_액세스_토큰을_생성한다() {
            // when
            String token = authTokenProvider.generateAccessToken(siteUser);

            // then
            String actualSubject = JwtUtils.parseSubject(token, jwtProperties.secret());
            assertThat(actualSubject).isEqualTo(subject);
        }

        @Test
        void subject_로_액세스_토큰을_생성한다() {
            // given
            String subject = "subject123";

            // when
            String token = authTokenProvider.generateAccessToken(subject);

            // then
            String actualSubject = JwtUtils.parseSubject(token, jwtProperties.secret());
            assertThat(actualSubject).isEqualTo(subject);
        }
    }

    @Nested
    class 리프레시_토큰을_제공한다 {

        @Test
        void SiteUser_로_리프레시_토큰을_생성하고_저장한다() {
            // when
            String refreshToken = authTokenProvider.generateAndSaveRefreshToken(siteUser);

            // then
            String actualSubject = JwtUtils.parseSubject(refreshToken, jwtProperties.secret());
            String refreshTokenKey = TokenType.REFRESH.addPrefix(subject);
            assertAll(
                    () -> assertThat(actualSubject).isEqualTo(subject),
                    () -> assertThat(redisTemplate.opsForValue().get(refreshTokenKey)).isEqualTo(refreshToken)
            );
        }

        @Test
        void 저장된_리프레시_토큰을_조회한다() {
            // given
            String refreshToken = "refreshToken";
            redisTemplate.opsForValue().set(TokenType.REFRESH.addPrefix(subject), refreshToken);

            // when
            Optional<String> optionalRefreshToken = authTokenProvider.findRefreshToken(subject);

            // then
            assertThat(optionalRefreshToken.get()).isEqualTo(refreshToken);
        }

        @Test
        void 저장되지_않은_리프레시_토큰을_조회한다() {
            // when
            Optional<String> optionalRefreshToken = authTokenProvider.findRefreshToken(subject);

            // then
            assertThat(optionalRefreshToken).isEmpty();
        }
    }

    @Nested
    class 블랙리스트_토큰을_제공한다 {

        @Test
        void 엑세스_토큰으로_블랙리스트_토큰을_생성하고_저장한다() {
            // when
            String accessToken = "accessToken";
            String blackListToken = authTokenProvider.generateAndSaveBlackListToken(accessToken);

            // then
            String actualSubject = JwtUtils.parseSubject(blackListToken, jwtProperties.secret());
            String blackListTokenKey = TokenType.BLACKLIST.addPrefix(accessToken);
            assertAll(
                    () -> assertThat(actualSubject).isEqualTo(accessToken),
                    () -> assertThat(redisTemplate.opsForValue().get(blackListTokenKey)).isEqualTo(blackListToken)
            );
        }

        @Test
        void 저장된_블랙리스트_토큰을_조회한다() {
            // given
            String accessToken = "accessToken";
            String blackListToken = "token";
            redisTemplate.opsForValue().set(TokenType.BLACKLIST.addPrefix(accessToken), blackListToken);

            // when
            Optional<String> optionalBlackListToken = authTokenProvider.findBlackListToken(accessToken);

            // then
            assertThat(optionalBlackListToken).hasValue(blackListToken);
        }

        @Test
        void 저장되지_않은_블랙리스트_토큰을_조회한다() {
            // when
            Optional<String> optionalBlackListToken = authTokenProvider.findBlackListToken("accessToken");

            // then
            assertThat(optionalBlackListToken).isEmpty();
        }
    }

    @Test
    void 토큰을_생성한다() {
        // when
        String subject = "subject123";
        String token = authTokenProvider.generateToken(subject, TokenType.ACCESS);

        // then
        String extractedSubject = Jwts.parser()
                .setSigningKey(jwtProperties.secret())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        assertThat(subject).isEqualTo(extractedSubject);
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
