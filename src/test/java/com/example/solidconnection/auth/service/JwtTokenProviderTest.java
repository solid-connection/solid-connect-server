package com.example.solidconnection.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.auth.token.JwtTokenProvider;
import com.example.solidconnection.auth.token.config.JwtProperties;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import io.jsonwebtoken.Claims;
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

@DisplayName("토큰 제공자 테스트")
@TestContainerSpringBootTest
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void 토큰을_생성한다() {
        // given
        String actualSubject = "subject123";
        TokenType actualTokenType = TokenType.ACCESS;

        // when
        String token = tokenProvider.generateToken(actualSubject, actualTokenType);

        // then - subject와 만료 시간이 일치하는지 검증
        Claims claims = tokenProvider.parseClaims(token);
        long expectedExpireTime = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertAll(
                () -> assertThat(claims.getSubject()).isEqualTo(actualSubject),
                () -> assertThat(expectedExpireTime).isEqualTo(actualTokenType.getExpireTime())
        );
    }

    @Test
    void 토큰을_저장한다() {
        // given
        String subject = "subject123";
        TokenType tokenType = TokenType.ACCESS;
        String token = tokenProvider.generateToken(subject, tokenType);

        // when
        String savedToken = tokenProvider.saveToken(token, tokenType);

        // then - key: "{TokenType.Prefix}:subject", value: {token} 로 저장되어있는지 검증, 반환하는 값이 value와 같은지 검증
        String key = tokenType.addPrefix(subject);
        String value = redisTemplate.opsForValue().get(key);
        assertAll(
                () -> assertThat(value).isEqualTo(token),
                () -> assertThat(savedToken).isEqualTo(value)
        );
    }

    @Nested
    class 토큰으로부터_subject_를_추출한다 {

        @Test
        void 유효한_토큰의_subject_를_추출한다() {
            // given
            String subject = "subject000";
            String token = createValidToken(subject);

            // when
            String extractedSubject = tokenProvider.parseSubject(token);

            // then
            assertThat(extractedSubject).isEqualTo(subject);
        }

        @Test
        void 유효하지_않은_토큰의_subject_를_추출하면_예외_응답을_반환한다() {
            // given
            String subject = "subject123";
            String token = createExpiredToken(subject);

            // when, then
            assertThatCode(() -> tokenProvider.parseSubject(token))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.INVALID_TOKEN.getMessage());
        }
    }

    @Nested
    class 토큰으로부터_claim_을_추출한다 {

        @Test
        void 유효한_토큰의_claim_을_추출한다() {
            // given
            String subject = "subject";
            String claimKey = "key";
            String claimValue = "value";
            Claims expectedClaims = Jwts.claims(new HashMap<>(Map.of(claimKey, claimValue))).setSubject(subject);
            String token = createValidToken(expectedClaims);

            // when
            Claims actualClaims = tokenProvider.parseClaims(token);

            // then
            assertAll(
                    () -> assertThat(actualClaims.getSubject()).isEqualTo(subject),
                    () -> assertThat(actualClaims.get(claimKey)).isEqualTo(claimValue)
            );
        }

        @Test
        void 유효하지_않은_토큰의_claim_을_추출하면_예외_응답을_반환한다() {
            // given
            String subject = "subject";
            Claims expectedClaims = Jwts.claims().setSubject(subject);
            String token = createExpiredToken(expectedClaims);

            // when
            assertThatCode(() -> tokenProvider.parseClaims(token))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.INVALID_TOKEN.getMessage());
        }
    }

    private String createValidToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.secret())
                .compact();
    }

    private String createValidToken(Claims claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.secret())
                .compact();
    }

    private String createExpiredToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.secret())
                .compact();
    }

    private String createExpiredToken(Claims claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.secret())
                .compact();
    }
}
