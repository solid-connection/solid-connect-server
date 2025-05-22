package com.example.solidconnection.auth.service.oauth;

import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.auth.service.TokenProvider;
import com.example.solidconnection.auth.token.config.JwtProperties;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.AuthType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.example.solidconnection.common.exception.ErrorCode.SIGN_UP_TOKEN_INVALID;
import static com.example.solidconnection.common.exception.ErrorCode.SIGN_UP_TOKEN_NOT_ISSUED_BY_SERVER;

@Component
@RequiredArgsConstructor
public class OAuthSignUpTokenProvider {

    static final String AUTH_TYPE_CLAIM_KEY = "authType";

    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, String> redisTemplate;
    private final TokenProvider tokenProvider;

    public String generateAndSaveSignUpToken(String email, AuthType authType) {
        Map<String, Object> authTypeClaim = new HashMap<>(Map.of(AUTH_TYPE_CLAIM_KEY, authType));
        Claims claims = Jwts.claims(authTypeClaim).setSubject(email);
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + TokenType.SIGN_UP.getExpireTime());

        String signUpToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS512, jwtProperties.secret())
                .compact();
        return tokenProvider.saveToken(signUpToken, TokenType.SIGN_UP);
    }

    public void validateSignUpToken(String token) {
        validateFormatAndExpiration(token);
        String email = parseEmail(token);
        validateIssuedByServer(email);
    }

    private void validateFormatAndExpiration(String token) {
        try {
            Claims claims = tokenProvider.parseClaims(token);
            Objects.requireNonNull(claims.getSubject());
            String serializedAuthType = claims.get(AUTH_TYPE_CLAIM_KEY, String.class);
            AuthType.valueOf(serializedAuthType);
        } catch (Exception e) {
            throw new CustomException(SIGN_UP_TOKEN_INVALID);
        }
    }

    private void validateIssuedByServer(String email) {
        String key = TokenType.SIGN_UP.addPrefix(email);
        if (redisTemplate.opsForValue().get(key) == null) {
            throw new CustomException(SIGN_UP_TOKEN_NOT_ISSUED_BY_SERVER);
        }
    }

    public String parseEmail(String token) {
        return tokenProvider.parseSubject(token);
    }

    public AuthType parseAuthType(String token) {
        Claims claims = tokenProvider.parseClaims(token);
        String authTypeStr = claims.get(AUTH_TYPE_CLAIM_KEY, String.class);
        return AuthType.valueOf(authTypeStr);
    }
}
