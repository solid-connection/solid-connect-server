package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.siteuser.domain.SiteUser;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.example.solidconnection.util.JwtUtils.parseSubjectIgnoringExpiration;

@Component
public class AuthTokenProvider extends TokenProvider {

    public AuthTokenProvider(JwtProperties jwtProperties, RedisTemplate<String, String> redisTemplate) {
        super(jwtProperties, redisTemplate);
    }

    public String generateAccessToken(SiteUser siteUser) {
        String subject = siteUser.getId().toString();
        return generateToken(subject, TokenType.ACCESS);
    }

    public String generateAccessToken(String subject) {
        return generateToken(subject, TokenType.ACCESS);
    }

    public String generateAndSaveRefreshToken(SiteUser siteUser) {
        String subject = siteUser.getId().toString();
        String refreshToken = generateToken(subject, TokenType.REFRESH);
        return saveToken(refreshToken, TokenType.REFRESH);
    }

    public String generateAndSaveBlackListToken(String accessToken) {
        String refreshToken = generateToken(accessToken, TokenType.BLACKLIST);
        return saveToken(refreshToken, TokenType.BLACKLIST);
    }

    public Optional<String> findRefreshToken(String subject) {
        String refreshTokenKey = TokenType.REFRESH.addPrefixToSubject(subject);
        return Optional.ofNullable(redisTemplate.opsForValue().get(refreshTokenKey));
    }

    public Optional<String> findBlackListToken(String subject) {
        String refreshTokenKey = TokenType.BLACKLIST.addPrefixToSubject(subject);
        return Optional.ofNullable(redisTemplate.opsForValue().get(refreshTokenKey));
    }

    public String getEmail(String token) {
        return parseSubjectIgnoringExpiration(token, jwtProperties.secret());
    }
}
