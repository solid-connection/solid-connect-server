package com.example.solidconnection.config.token;

import com.example.solidconnection.custom.userdetails.CustomUserDetailsService;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TokenService {
    private final RedisTemplate<String, String> redisTemplate;
    private final SiteUserRepository siteUserRepository;
    private final CustomUserDetailsService customUserDetailsService;

    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(String email, TokenType tokenType) {
        Claims claims = Jwts.claims().setSubject(email);
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + tokenType.getExpireTime());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS512, this.secretKey)
                .compact();
    }

    public void saveToken(String token, TokenType tokenType) {
        redisTemplate.opsForValue().set(
                tokenType.createTokenKey(getClaim(token).getSubject()),
                token,
                tokenType.getExpireTime(),
                TimeUnit.MILLISECONDS
        );
    }

    public Authentication getAuthentication(String token) {
        String email = getClaim(token).getSubject();
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getEmail(String token) {
        return getClaim(token).getSubject();
    }

    private Claims getClaim(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
