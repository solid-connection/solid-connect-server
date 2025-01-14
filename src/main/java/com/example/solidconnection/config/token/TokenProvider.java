package com.example.solidconnection.config.token;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.userdetails.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_TOKEN;

@RequiredArgsConstructor
@Component
public class TokenProvider {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final RedisTemplate<String, String> redisTemplate;
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

    public String saveToken(String token, TokenType tokenType) {
        String subject = parseSubjectOrElseThrow(token);
        redisTemplate.opsForValue().set(
                tokenType.addPrefixToSubject(subject),
                token,
                tokenType.getExpireTime(),
                TimeUnit.MILLISECONDS
        );
        return token;
    }

    public Authentication getAuthentication(String token) {
        String email = parseSubject(token);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getEmail(String token) {
        return parseSubject(token);
    }

    public String parseTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);
        if (token == null || token.isBlank() || !token.startsWith(TOKEN_PREFIX)) {
            return null;
        }
        return token.substring(TOKEN_PREFIX.length());
    }

    public String parseSubject(String token) {
        try {
            return extractSubject(token);
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        }
    }

    public String parseSubjectOrElseThrow(String token) {
        try {
            return extractSubject(token);
        } catch (ExpiredJwtException e) {
            throw new CustomException(INVALID_TOKEN);
        }
    }

    private String extractSubject(String token) throws ExpiredJwtException {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
