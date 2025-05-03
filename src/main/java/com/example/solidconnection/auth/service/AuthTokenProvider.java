package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.siteuser.domain.SiteUser;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthTokenProvider extends TokenProvider {

    public AuthTokenProvider(JwtProperties jwtProperties, RedisTemplate<String, String> redisTemplate) {
        super(jwtProperties, redisTemplate);
    }

    public String generateAccessToken(SiteUser siteUser) {
        String subject = getSubject(siteUser);
        return generateToken(subject, TokenType.ACCESS);
    }

    public String generateAccessToken(String subject) {
        return generateToken(subject, TokenType.ACCESS);
    }

    public String generateAndSaveRefreshToken(SiteUser siteUser) {
        String subject = getSubject(siteUser);
        String refreshToken = generateToken(subject, TokenType.REFRESH);
        return saveToken(refreshToken, TokenType.REFRESH);
    }

    public String generateAndSaveBlackListToken(String accessToken) {
        String blackListToken = generateToken(accessToken, TokenType.BLACKLIST);
        return saveToken(blackListToken, TokenType.BLACKLIST);
    }

    public Optional<String> findRefreshToken(String subject) {
        String refreshTokenKey = TokenType.REFRESH.addPrefix(subject);
        return Optional.ofNullable(redisTemplate.opsForValue().get(refreshTokenKey));
    }

    public Optional<String> findBlackListToken(String subject) {
        String blackListTokenKey = TokenType.BLACKLIST.addPrefix(subject);
        return Optional.ofNullable(redisTemplate.opsForValue().get(blackListTokenKey));
    }

    private String getSubject(SiteUser siteUser) {
        return siteUser.getId().toString();
    }
}
