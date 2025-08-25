package com.example.solidconnection.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.auth.domain.Subject;
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

@DisplayName("토큰 제공자 테스트")
@TestContainerSpringBootTest
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private JwtProperties jwtProperties;

    private final Subject expectedSubject = new Subject("subject123");
    private final long expectedExpireTime = 10000L;

    @Nested
    class 토큰을_생성한다 {

        @Test
        void subject_만_있는_토큰을_생성한다() {
            // when
            String token = tokenProvider.generateToken(expectedSubject, expectedExpireTime);

            // then - subject와 만료 시간이 일치하는지 검증
            Subject actualSubject = tokenProvider.parseSubject(token);
            long actualExpireTime = getActualExpireTime(token);
            assertAll(
                    () -> assertThat(actualSubject).isEqualTo(expectedSubject),
                    () -> assertThat(actualExpireTime).isEqualTo(expectedExpireTime)
            );
        }

        @Test
        void subject_와_claims_가_있는_토큰을_생성한다() {
            // given
            String key1 = "key1";
            String value1 = "value1";
            String key2 = "key2";
            String value2 = "value2";
            Map<String, String> customClaims = Map.of(key1, value1, key2, value2);

            // when
            String token = tokenProvider.generateToken(expectedSubject, customClaims, expectedExpireTime);

            // then - subject와 커스텀 클레임이 일치하는지 검증
            Subject actualSubject = tokenProvider.parseSubject(token);
            long actualExpireTime = getActualExpireTime(token);
            assertAll(
                    () -> assertThat(actualSubject).isEqualTo(expectedSubject),
                    () -> assertThat(actualExpireTime).isEqualTo(expectedExpireTime),
                    () -> assertThat(tokenProvider.parseClaims(token, key1, String.class)).isEqualTo(value1),
                    () -> assertThat(tokenProvider.parseClaims(token, key2, String.class)).isEqualTo(value2)
            );
        }

        private long getActualExpireTime(String token) {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtProperties.secret())
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        }
    }

    @Nested
    class 토큰으로부터_subject_를_추출한다 {

        @Test
        void 유효한_토큰의_subject_를_추출한다() {
            // given
            String token = tokenProvider.generateToken(expectedSubject, expectedExpireTime);

            // when
            Subject actualSubject = tokenProvider.parseSubject(token);

            // then
            assertThat(actualSubject).isEqualTo(expectedSubject);
        }

        @Test
        void 유효하지_않은_토큰의_subject_를_추출하면_예외가_발생한다() {
            // given
            String subject = "subject123";
            String token = createExpiredToken(subject);

            // when, then
            assertThatCode(() -> tokenProvider.parseSubject(token))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.INVALID_TOKEN.getMessage());
        }

        @Test
        void subject_가_없는_토큰의_subject_를_추출하면_예외가_발생한다() {
            // given
            Claims claims = Jwts.claims(new HashMap<>());
            String subjectNotExistingToken = createExpiredToken(claims);
            String subjectBlankToken = tokenProvider.generateToken(new Subject("   "), expectedExpireTime);

            // when, then
            assertAll(
                    () -> assertThatCode(() -> tokenProvider.parseSubject(subjectNotExistingToken))
                            .isInstanceOf(CustomException.class)
                            .hasMessage(ErrorCode.INVALID_TOKEN.getMessage()),
                    () -> assertThatCode(() -> tokenProvider.parseSubject(subjectBlankToken))
                            .isInstanceOf(CustomException.class)
                            .hasMessage(ErrorCode.INVALID_TOKEN.getMessage())
            );
        }
    }

    @Nested
    class 토큰으로부터_claim_을_추출한다 {

        private final String claimKey = "key";
        private final String claimValue = "value";

        @Test
        void 유효한_토큰의_claim_을_추출한다() {
            // given
            String token = tokenProvider.generateToken(
                    expectedSubject,
                    Map.of(claimKey, claimValue),
                    expectedExpireTime
            );

            // when
            String actualClaimValue = tokenProvider.parseClaims(token, claimKey, String.class);

            // then
            assertThat(actualClaimValue).isEqualTo(claimValue);
        }

        @Test
        void 유효하지_않은_토큰의_claim_을_추출하면_예외가_발생한다() {
            // given
            Claims expectedClaims = Jwts.claims(new HashMap<>(Map.of(claimKey, claimValue)));
            String token = createExpiredToken(expectedClaims);

            // when
            assertThatCode(() -> tokenProvider.parseClaims(token, claimKey, String.class))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.INVALID_TOKEN.getMessage());
        }

        @Test
        void 존재하지_않는_claim_을_추출하면_null을_반환한다() {
            // given
            String token = tokenProvider.generateToken(
                    expectedSubject,
                    Map.of(claimKey, claimValue),
                    expectedExpireTime
            );
            String nonExistentClaimKey = "nonExistentKey";

            // when
            String actualClaimValue = tokenProvider.parseClaims(token, nonExistentClaimKey, String.class);

            // then
            assertThat(actualClaimValue).isNull();
        }
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
