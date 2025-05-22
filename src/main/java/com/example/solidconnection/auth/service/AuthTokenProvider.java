package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AuthTokenProvider {

    private final RedisTemplate<String, String> redisTemplate;
    private final TokenProvider tokenProvider;

    public AccessToken generateAccessToken(Subject subject) {
        String token = tokenProvider.generateToken(subject.value(), TokenType.ACCESS);
        return new AccessToken(subject, token);
    }

    public RefreshToken generateAndSaveRefreshToken(Subject subject) {
        String token = tokenProvider.generateToken(subject.value(), TokenType.REFRESH);
        tokenProvider.saveToken(token, TokenType.REFRESH);
        return new RefreshToken(subject, token);
    }

    /*
     * 유효한 리프레시 토큰인지 확인한다.
     * - 요청된 토큰과 같은 subject 의 리프레시 토큰을 조회한다.
     * - 조회된 리프레시 토큰과 요청된 토큰이 같은지 비교한다.
     * */
    public boolean isValidRefreshToken(String requestedRefreshToken) {
        String subject = tokenProvider.parseSubject(requestedRefreshToken);
        String refreshTokenKey = TokenType.REFRESH.addPrefix(subject);
        String foundRefreshToken = redisTemplate.opsForValue().get(refreshTokenKey);
        return Objects.equals(requestedRefreshToken, foundRefreshToken);
    }

    public void deleteRefreshTokenByAccessToken(AccessToken accessToken) {
        String subject = accessToken.subject().value();
        String refreshTokenKey = TokenType.REFRESH.addPrefix(subject);
        redisTemplate.delete(refreshTokenKey);
    }

    public Subject parseSubject(String token) {
        String subject = tokenProvider.parseSubject(token);
        return new Subject(subject);
    }

    public Subject toSubject(SiteUser siteUser) {
        return new Subject(siteUser.getId().toString());
    }

    public AccessToken toAccessToken(String token) {
        return new AccessToken(parseSubject(token), token);
    }
}
