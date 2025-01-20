package com.example.solidconnection.config.token;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.auth.service.TokenProvider;
import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@TestContainerSpringBootTest
@DisplayName("TokenProvider 테스트")
class TokenProviderTest {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void 토큰을_생성한다() {
        // when
        String subject = "subject123";
        String token = tokenProvider.generateToken(subject, TokenType.ACCESS);

        // then
        String extractedSubject = Jwts.parser()
                .setSigningKey(jwtProperties.secret())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        assertThat(subject).isEqualTo(extractedSubject);
    }

    @Nested
    class 토큰을_저장한다 {

        @Test
        void 토큰이_유효하면_저장한다() {
            // given
            String subject = "subject321";
            String token = createValidToken(subject);

            // when
            tokenProvider.saveToken(token, TokenType.ACCESS);

            // then
            String savedToken = redisTemplate.opsForValue().get(TokenType.ACCESS.addPrefixToSubject(subject));
            assertThat(savedToken).isEqualTo(token);
        }

        @Test
        void 토큰이_유효하지않으면_예외가_발생한다() {
            // given
            String token = createInvalidToken();

            // when & then
            assertThatCode(() -> tokenProvider.saveToken(token, TokenType.REFRESH))
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

    private String createInvalidToken() {
        return Jwts.builder()
                .setSubject("subject")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.secret())
                .compact();
    }
}
