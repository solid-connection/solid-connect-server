package com.example.solidconnection.auth.service.oauth;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.solidconnection.auth.service.oauth.OAuthSignUpTokenProvider.AUTH_TYPE_CLAIM_KEY;
import static com.example.solidconnection.custom.exception.ErrorCode.SIGN_UP_TOKEN_INVALID;
import static com.example.solidconnection.custom.exception.ErrorCode.SIGN_UP_TOKEN_NOT_ISSUED_BY_SERVER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestContainerSpringBootTest
@DisplayName("OAuth 회원가입 토큰 제공자 테스트")
class OAuthSignUpTokenProviderTest {

    @Autowired
    private OAuthSignUpTokenProvider OAuthSignUpTokenProvider;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void 회원가입_토큰을_생성하고_저장한다() {
        // given
        String email = "email";
        AuthType authType = AuthType.KAKAO;

        // when
        String signUpToken = OAuthSignUpTokenProvider.generateAndSaveSignUpToken(email, authType);

        // then
        Claims claims = JwtUtils.parseClaims(signUpToken, jwtProperties.secret());
        String actualSubject = claims.getSubject();
        AuthType actualAuthType = AuthType.valueOf(claims.get(AUTH_TYPE_CLAIM_KEY, String.class));
        String signUpTokenKey = TokenType.SIGN_UP.addPrefix(email);
        assertAll(
                () -> assertThat(actualSubject).isEqualTo(email),
                () -> assertThat(actualAuthType).isEqualTo(authType),
                () -> assertThat(redisTemplate.opsForValue().get(signUpTokenKey)).isEqualTo(signUpToken)
        );
    }

    @Nested
    class 주어진_회원가입_토큰을_검증한다 {

        @Test
        void 검증_성공한다() {
            // given
            String email = "email@test.com";
            Map<String, Object> claim = new HashMap<>(Map.of(AUTH_TYPE_CLAIM_KEY, AuthType.APPLE));
            String validToken = createBaseJwtBuilder().setSubject(email).addClaims(claim).compact();
            redisTemplate.opsForValue().set(TokenType.SIGN_UP.addPrefix(email), validToken);

            // when & then
            assertThatCode(() -> OAuthSignUpTokenProvider.validateSignUpToken(validToken)).doesNotThrowAnyException();
        }

        @Test
        void 만료되었으면_예외_응답을_반환한다() {
            // given
            String expiredToken = createExpiredToken();

            // when & then
            assertThatCode(() -> OAuthSignUpTokenProvider.validateSignUpToken(expiredToken))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(SIGN_UP_TOKEN_INVALID.getMessage());
        }

        @Test
        void 정해진_형식에_맞지_않으면_예외_응답을_반환한다_jwt_가_아닌_토큰() {
            // given
            String notJwt = "not jwt";

            // when & then
            assertThatCode(() -> OAuthSignUpTokenProvider.validateSignUpToken(notJwt))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(SIGN_UP_TOKEN_INVALID.getMessage());
        }

        @Test
        void 정해진_형식에_맞지_않으면_예외_응답을_반환한다_authType_클래스_불일치() {
            // given
            Map<String, Object> wrongClaim = new HashMap<>(Map.of(AUTH_TYPE_CLAIM_KEY, "카카오"));
            String wrongAuthType = createBaseJwtBuilder().addClaims(wrongClaim).compact();

            // when & then
            assertThatCode(() -> OAuthSignUpTokenProvider.validateSignUpToken(wrongAuthType))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(SIGN_UP_TOKEN_INVALID.getMessage());
        }

        @Test
        void 정해진_형식에_맞지_않으면_예외_응답을_반환한다_subject_누락() {
            // given
            Map<String, Object> claim = new HashMap<>(Map.of(AUTH_TYPE_CLAIM_KEY, AuthType.APPLE));
            String noSubject = createBaseJwtBuilder().addClaims(claim).compact();

            // when & then
            assertThatCode(() -> OAuthSignUpTokenProvider.validateSignUpToken(noSubject))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(SIGN_UP_TOKEN_INVALID.getMessage());
        }

        @Test
        void 우리_서버에_발급된_토큰이_아니면_예외_응답을_반환한다() {
            // given
            Map<String, Object> validClaim = new HashMap<>(Map.of(AUTH_TYPE_CLAIM_KEY, AuthType.APPLE));
            String signUpToken = createBaseJwtBuilder().addClaims(validClaim).setSubject("email").compact();

            // when & then
            assertThatCode(() -> OAuthSignUpTokenProvider.validateSignUpToken(signUpToken))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(SIGN_UP_TOKEN_NOT_ISSUED_BY_SERVER.getMessage());
        }
    }

    @Test
    void 회원가입_토큰에서_이메일을_추출한다() {
        // given
        String email = "email@test.com";
        Map<String, Object> claim = Map.of(AUTH_TYPE_CLAIM_KEY, AuthType.APPLE);
        String validToken = createBaseJwtBuilder().setSubject(email).addClaims(claim).compact();
        redisTemplate.opsForValue().set(TokenType.SIGN_UP.addPrefix(email), validToken);

        // when
        String extractedEmail = OAuthSignUpTokenProvider.parseEmail(validToken);

        // then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    void 회원가입_토큰에서_인증_타입을_추출한다() {
        // given
        AuthType authType = AuthType.APPLE;
        Map<String, Object> claim = Map.of(AUTH_TYPE_CLAIM_KEY, authType);
        String validToken = createBaseJwtBuilder().setSubject("email").addClaims(claim).compact();

        // when
        AuthType extractedAuthType = OAuthSignUpTokenProvider.parseAuthType(validToken);

        // then
        assertThat(extractedAuthType).isEqualTo(authType);
    }
    
    private String createExpiredToken() {
        return Jwts.builder()
                .setSubject("subject")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.secret())
                .compact();
    }

    private JwtBuilder createBaseJwtBuilder() {
        return Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.secret());
    }
}
