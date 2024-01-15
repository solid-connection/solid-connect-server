/*
package com.example.solidconnection.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1시간
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 14; // 2주

    private final RedisTemplate<String, String> redisTemplate;
    private final SiteUserRepository siteUserRepository;

    @Value("{spring.jwt.secret}")
    private String secretKey;

    public String generateAccessToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);

        var now = new Date();
        var expiredDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setClaims(claims) // 발행 유저 정보 저장
                .setIssuedAt(now) // 토큰 생성 시간
                .setExpiration(expiredDate) // 토큰 만료 시간
                .signWith(SignatureAlgorithm.HS512, this.secretKey) // 사용할 암호화 알고리즘, 비밀키
                .compact(); // 생성
    }

    public String generateAndSaveRefreshToken(String email) {

        RefreshToken refreshToken = new RefreshToken(email, UUID.randomUUID().toString());
        String token = refreshToken.getRefreshToken();

        redisTemplate.opsForValue().set(
                email, // redis에서 사용할 key
                token, // redis에서 사용할 value
                REFRESH_TOKEN_EXPIRE_TIME, // refresh token이 저장되는 기간
                TimeUnit.MILLISECONDS
        );
        return token;
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = siteUserRepository.findByEmail(this.getUserEmail(token))
                .orElseThrow(() -> new RacketPuncherException(EMAIL_NOT_FOUND));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUserEmail(String token) {
        return this.parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        validateRefreshToken(token);
        var claims = this.parseClaims(token);
        return !claims.getExpiration().before(new Date());
    }

    private void validateRefreshToken(String token) {
        if (token.equals(redisTemplate.opsForValue().get(getUserEmail(token)))) {
            throw new RacketPuncherException(TOKEN_EXPIRED);
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            log.error("만료된 토큰입니다.");
            return e.getClaims();
        }
    }

    public Long getExpiration(String token) {
        Date expiration = Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody().getExpiration();
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }
}
*/
