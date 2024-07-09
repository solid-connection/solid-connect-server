package com.example.solidconnection.config.token;

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

import static com.example.solidconnection.custom.exception.ErrorCode.ACCESS_TOKEN_EXPIRED;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_SERVICE_PUBLISHED_KAKAO_TOKEN;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_TOKEN;
import static com.example.solidconnection.custom.exception.ErrorCode.REFRESH_TOKEN_EXPIRED;
import static com.example.solidconnection.custom.exception.ErrorCode.USER_ALREADY_SIGN_OUT;

@Component
@RequiredArgsConstructor
public class TokenValidator {

    public static final String SIGN_OUT_VALUE = "signOut";

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret}")
    private String secretKey;

    public void validateAccessToken(String token) {
        validateTokenNotEmpty(token);
        validateTokenNotExpired(token, TokenType.ACCESS);
        validateNotSignOut(token);
        validateRefreshToken(token);
    }

    private void validateRefreshToken(String token) {
        String email = getClaim(token).getSubject();
        if (redisTemplate.opsForValue().get(TokenType.REFRESH.addTokenPrefixToSubject(email)) == null) {
            throw new CustomException(REFRESH_TOKEN_EXPIRED);
        }
    }

    private void validateNotSignOut(String token) {
        String email = getClaim(token).getSubject();
        if (SIGN_OUT_VALUE.equals(redisTemplate.opsForValue().get(TokenType.REFRESH.addTokenPrefixToSubject(email)))) {
            throw new CustomException(USER_ALREADY_SIGN_OUT);
        }
    }

    public void validateKakaoToken(String token) {
        validateTokenNotEmpty(token);
        validateTokenNotExpired(token, TokenType.KAKAO_OAUTH);
        validateKakaoTokenNotUsed(token);
    }

    private void validateKakaoTokenNotUsed(String token) {
        String email = getClaim(token).getSubject();
        if (!Objects.equals(redisTemplate.opsForValue().get(TokenType.KAKAO_OAUTH.addTokenPrefixToSubject(email)), token)) {
            throw new CustomException(INVALID_SERVICE_PUBLISHED_KAKAO_TOKEN);
        }
    }

    private void validateTokenNotExpired(String token, TokenType tokenType) {
        Date expiration = getClaim(token).getExpiration();
        long now = new Date().getTime();
        if ((expiration.getTime() - now) < 0) {
            if (tokenType.equals(TokenType.ACCESS)) {
                throw new CustomException(ACCESS_TOKEN_EXPIRED);
            }
            if (token.equals(TokenType.KAKAO_OAUTH)) {
                throw new CustomException(INVALID_SERVICE_PUBLISHED_KAKAO_TOKEN);
            }
        }
    }

    private void validateTokenNotEmpty(String token) {
        if (!StringUtils.hasText(token)) {
            throw new CustomException(INVALID_TOKEN);
        }
    }

    private Claims getClaim(String token) {
        return Jwts.parser()
                .setSigningKey(this.secretKey)
                .parseClaimsJws(token)
                .getBody();
    }
}
