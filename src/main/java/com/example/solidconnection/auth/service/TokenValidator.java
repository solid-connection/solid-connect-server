package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.custom.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Objects;

import static com.example.solidconnection.auth.domain.TokenType.ACCESS;
import static com.example.solidconnection.auth.domain.TokenType.REFRESH;
import static com.example.solidconnection.auth.domain.TokenType.SIGN_UP;
import static com.example.solidconnection.custom.exception.ErrorCode.ACCESS_TOKEN_EXPIRED;
import static com.example.solidconnection.custom.exception.ErrorCode.EMPTY_TOKEN;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_SERVICE_PUBLISHED_KAKAO_TOKEN;
import static com.example.solidconnection.custom.exception.ErrorCode.REFRESH_TOKEN_EXPIRED;

@Component
@RequiredArgsConstructor
public class TokenValidator {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret}")
    private String secretKey;

    public void validateAccessToken(String token) {
        validateTokenNotEmpty(token);
        validateTokenNotExpired(token, ACCESS);
        validateRefreshToken(token);
    }

    public void validateKakaoToken(String token) {
        validateTokenNotEmpty(token);
        validateTokenNotExpired(token, SIGN_UP);
        validateKakaoTokenNotUsed(token);
    }

    private void validateTokenNotEmpty(String token) {
        if (!StringUtils.hasText(token)) {
            throw new CustomException(EMPTY_TOKEN);
        }
    }

    private void validateTokenNotExpired(String token, TokenType tokenType) {
        Date expiration = getClaim(token).getExpiration();
        long now = new Date().getTime();
        if ((expiration.getTime() - now) < 0) {
            if (tokenType.equals(ACCESS)) {
                throw new CustomException(ACCESS_TOKEN_EXPIRED);
            }
            if (token.equals(SIGN_UP)) {
                throw new CustomException(INVALID_SERVICE_PUBLISHED_KAKAO_TOKEN);
            }
        }
    }

    private void validateRefreshToken(String token) {
        String email = getClaim(token).getSubject();
        if (redisTemplate.opsForValue().get(REFRESH.addPrefix(email)) == null) {
            throw new CustomException(REFRESH_TOKEN_EXPIRED);
        }
    }

    private void validateKakaoTokenNotUsed(String token) {
        String email = getClaim(token).getSubject();
        if (!Objects.equals(redisTemplate.opsForValue().get(SIGN_UP.addPrefix(email)), token)) {
            throw new CustomException(INVALID_SERVICE_PUBLISHED_KAKAO_TOKEN);
        }
    }

    private Claims getClaim(String token) {
        return Jwts.parser()
                .setSigningKey(this.secretKey)
                .parseClaimsJws(token)
                .getBody();
    }
}
