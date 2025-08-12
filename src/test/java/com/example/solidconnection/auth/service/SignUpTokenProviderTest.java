package com.example.solidconnection.auth.service;

import static com.example.solidconnection.common.exception.ErrorCode.SIGN_UP_TOKEN_INVALID;
import static com.example.solidconnection.common.exception.ErrorCode.SIGN_UP_TOKEN_NOT_ISSUED_BY_SERVER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.auth.token.config.JwtProperties;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

@TestContainerSpringBootTest
@DisplayName("회원가입 토큰 제공자 테스트")
class SignUpTokenProviderTest {

    @Autowired
    private SignUpTokenProvider signUpTokenProvider;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtProperties jwtProperties;

    private final String authTypeClaimKey = "authType";
    private final String email = "test@email.com";
    private final AuthType authType = AuthType.KAKAO;

    @Test
    void 회원가입_토큰을_생성하고_저장한다() {
        // when
        String signUpToken = signUpTokenProvider.generateAndSaveSignUpToken(email, authType);

        // then
        Claims claims = tokenProvider.parseClaims(signUpToken);
        String actualSubject = claims.getSubject();
        AuthType actualAuthType = AuthType.valueOf(claims.get(authTypeClaimKey, String.class));
        String signUpTokenKey = TokenType.SIGN_UP.addPrefix(email);
        assertAll(
                () -> assertThat(actualSubject).isEqualTo(email),
                () -> assertThat(actualAuthType).isEqualTo(authType),
                () -> assertThat(redisTemplate.opsForValue().get(signUpTokenKey)).isEqualTo(signUpToken)
        );
    }

    @Test
    void 회원가입_토큰을_삭제한다() {
        // given
        signUpTokenProvider.generateAndSaveSignUpToken(email, authType);

        // when
        signUpTokenProvider.deleteByEmail(email);

        // then
        String signUpTokenKey = TokenType.SIGN_UP.addPrefix(email);
        assertThat(redisTemplate.opsForValue().get(signUpTokenKey)).isNull();
    }

    @Nested
    class 주어진_회원가입_토큰을_검증한다 {

        @Test
        void 검증_성공한다() {
            // given
            Map<String, Object> claim = new HashMap<>(Map.of(authTypeClaimKey, authType));
            String validToken = createBaseJwtBuilder().setSubject(email).addClaims(claim).compact();
            redisTemplate.opsForValue().set(TokenType.SIGN_UP.addPrefix(email), validToken);

            // when & then
            assertThatCode(() -> signUpTokenProvider.validateSignUpToken(validToken)).doesNotThrowAnyException();
        }

        @Test
        void 만료되었으면_예외가_발생한다() {
            // given
            String expiredToken = createExpiredToken();

            // when & then
            assertThatCode(() -> signUpTokenProvider.validateSignUpToken(expiredToken))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(SIGN_UP_TOKEN_INVALID.getMessage());
        }

        @Test
        void 정해진_형식에_맞지_않으면_예외가_발생한다_jwt_가_아닌_토큰() {
            // given
            String notJwt = "not jwt";

            // when & then
            assertThatCode(() -> signUpTokenProvider.validateSignUpToken(notJwt))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(SIGN_UP_TOKEN_INVALID.getMessage());
        }

        @Test
        void 정해진_형식에_맞지_않으면_예외가_발생한다_authType_클래스_불일치() {
            // given
            String wrongAuthType = "카카오";
            Map<String, Object> wrongClaim = new HashMap<>(Map.of(authTypeClaimKey, wrongAuthType));
            String wrongAuthTypeClaim = createBaseJwtBuilder().addClaims(wrongClaim).compact();

            // when & then
            assertThatCode(() -> signUpTokenProvider.validateSignUpToken(wrongAuthTypeClaim))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(SIGN_UP_TOKEN_INVALID.getMessage());
        }

        @Test
        void 정해진_형식에_맞지_않으면_예외가_발생한다_subject_누락() {
            // given
            Map<String, Object> claim = new HashMap<>(Map.of(authTypeClaimKey, authType));
            String noSubject = createBaseJwtBuilder().addClaims(claim).compact();

            // when & then
            assertThatCode(() -> signUpTokenProvider.validateSignUpToken(noSubject))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(SIGN_UP_TOKEN_INVALID.getMessage());
        }

        @Test
        void 우리_서버에_발급된_토큰이_아니면_예외가_발생한다() {
            // given
            Map<String, Object> validClaim = new HashMap<>(Map.of(authTypeClaimKey, authType));
            String signUpToken = createBaseJwtBuilder().addClaims(validClaim).setSubject(email).compact();

            // when & then
            assertThatCode(() -> signUpTokenProvider.validateSignUpToken(signUpToken))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(SIGN_UP_TOKEN_NOT_ISSUED_BY_SERVER.getMessage());
        }
    }

    @Test
    void 회원가입_토큰에서_이메일을_추출한다() {
        // given
        Map<String, Object> claim = Map.of(authTypeClaimKey, authType);
        String validToken = createBaseJwtBuilder().setSubject(email).addClaims(claim).compact();
        redisTemplate.opsForValue().set(TokenType.SIGN_UP.addPrefix(email), validToken);

        // when
        String extractedEmail = signUpTokenProvider.parseEmail(validToken);

        // then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    void 회원가입_토큰에서_인증_타입을_추출한다() {
        // given
        Map<String, Object> claim = Map.of(authTypeClaimKey, authType);
        String validToken = createBaseJwtBuilder().setSubject(email).addClaims(claim).compact();

        // when
        AuthType extractedAuthType = signUpTokenProvider.parseAuthType(validToken);

        // then
        assertThat(extractedAuthType).isEqualTo(authType);
    }

    private String createExpiredToken() {
        return Jwts.builder()
                .setSubject(email)
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
