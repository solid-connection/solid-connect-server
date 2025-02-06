package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.auth.service.oauth.SignUpTokenProvider;
import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.util.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestContainerSpringBootTest
@DisplayName("회원가입 토큰 제공자 테스트")
class SignUpTokenProviderTest {

    @Autowired
    private SignUpTokenProvider signUpTokenProvider;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void 회원가입_토큰을_생성하고_저장한다() {
        // when
        String email = "email";
        String signUpToken = signUpTokenProvider.generateAndSaveSignUpToken(email);

        // then
        String actualSubject = JwtUtils.parseSubject(signUpToken, jwtProperties.secret());
        String signUpTokenKey = TokenType.SIGN_UP.addPrefix(email);
        assertAll(
                () -> assertThat(actualSubject).isEqualTo(email),
                () -> assertThat(redisTemplate.opsForValue().get(signUpTokenKey)).isEqualTo(signUpToken)
        );
    }

    @Test
    void 저장된_회원가입_토큰을_조회한다() {
        // given
        String email = "email";
        String signUpToken = "token";
        redisTemplate.opsForValue().set(TokenType.SIGN_UP.addPrefix(email), signUpToken);

        // when
        Optional<String> actualSignUpToken = signUpTokenProvider.findSignUpToken(email);

        // then
        assertThat(actualSignUpToken).hasValue(signUpToken);
    }

    @Test
    void 저장되지_않은_회원가입_토큰을_조회한다() {
        // given
        String email = "email";

        // when
        Optional<String> actualSignUpToken = signUpTokenProvider.findSignUpToken(email);

        // then
        assertThat(actualSignUpToken).isEmpty();
    }
}
