package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.config.security.JwtProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SignUpTokenProvider extends TokenProvider {

    public SignUpTokenProvider(JwtProperties jwtProperties, RedisTemplate<String, String> redisTemplate) {
        super(jwtProperties, redisTemplate);
    }

    public String generateAndSaveSignUpToken(String email) {
        String signUpToken = generateToken(email, TokenType.SIGN_UP);
        return saveToken(signUpToken, TokenType.SIGN_UP);
    }

    public Optional<String> findSignUpToken(String email) {
        String signUpKey = TokenType.SIGN_UP.addPrefixToSubject(email);
        return Optional.ofNullable(redisTemplate.opsForValue().get(signUpKey));
    }
}
