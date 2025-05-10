package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.util.JwtUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AuthTokenProvider extends TokenProvider implements BlacklistChecker {

    public AuthTokenProvider(JwtProperties jwtProperties, RedisTemplate<String, String> redisTemplate) {
        super(jwtProperties, redisTemplate);
    }

    public AccessToken generateAccessToken(Subject subject) {
        String token = generateToken(subject.value(), TokenType.ACCESS);
        return new AccessToken(subject, token);
    }

    public RefreshToken generateAndSaveRefreshToken(Subject subject) {
        String token = generateToken(subject.value(), TokenType.REFRESH);
        saveToken(token, TokenType.REFRESH);
        return new RefreshToken(subject, token);
    }

    /*
    * 액세스 토큰을 블랙리스트에 저장한다.
    * - key = BLACKLIST:{accessToken}
    * - value = "signOut" -> key 의 존재만 확인하므로, value 에는 무슨 값이 들어가도 상관없다.
    * */
    public void addToBlacklist(AccessToken accessToken) {
        String blackListKey = TokenType.BLACKLIST.addPrefix(accessToken.token());
        redisTemplate.opsForValue().set(blackListKey, "signOut");
    }

    /*
    * 유효한 리프레시 토큰인지 확인한다.
    * - 요청된 토큰과 같은 subject 의 리프레시 토큰을 조회한다.
    * - 조회된 리프레시 토큰과 요청된 토큰이 같은지 비교한다.
    * */
    public boolean isValidRefreshToken(String requestedRefreshToken) {
        String subject = JwtUtils.parseSubject(requestedRefreshToken, jwtProperties.secret());
        String refreshTokenKey = TokenType.REFRESH.addPrefix(subject);
        String foundRefreshToken = redisTemplate.opsForValue().get(refreshTokenKey);
        return Objects.equals(requestedRefreshToken, foundRefreshToken);
    }

    public void deleteRefreshTokenByAccessToken(AccessToken accessToken) {
        String subject = accessToken.subject().value();
        String refreshTokenKey = TokenType.REFRESH.addPrefix(subject);
        redisTemplate.delete(refreshTokenKey);
    }

    @Override
    public boolean isTokenBlacklisted(String accessToken) {
        String blackListTokenKey = TokenType.BLACKLIST.addPrefix(accessToken);
        return redisTemplate.hasKey(blackListTokenKey);
    }

    public Subject parseSubject(String token) {
        String subject = JwtUtils.parseSubject(token, jwtProperties.secret());
        return new Subject(subject);
    }

    public Subject toSubject(SiteUser siteUser) {
        return new Subject(siteUser.getId().toString());
    }

    public AccessToken toAccessToken(String token) {
        return new AccessToken(parseSubject(token), token);
    }
}
