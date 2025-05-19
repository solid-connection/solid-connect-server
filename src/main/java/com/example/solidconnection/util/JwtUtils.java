package com.example.solidconnection.util;

import com.example.solidconnection.common.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import static com.example.solidconnection.common.exception.ErrorCode.INVALID_TOKEN;

@Component
public class JwtUtils {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    public static String parseTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);
        if (token == null || token.isBlank() || !token.startsWith(TOKEN_PREFIX)) {
            return null;
        }
        return token.substring(TOKEN_PREFIX.length());
    }

    public static String parseSubject(String token, String secretKey) {
        try {
            return parseClaims(token, secretKey).getSubject();
        } catch (Exception e) {
            throw new CustomException(INVALID_TOKEN);
        }
    }

    public static Claims parseClaims(String token, String secretKey) throws ExpiredJwtException {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }
}
