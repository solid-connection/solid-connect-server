package com.example.solidconnection.util;

import com.example.solidconnection.custom.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_TOKEN;

@Component
public class JwtUtils {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private JwtUtils() {
    }

    public static String parseTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);
        if (token == null || token.isBlank() || !token.startsWith(TOKEN_PREFIX)) {
            return null;
        }
        return token.substring(TOKEN_PREFIX.length());
    }

    public static String parseSubjectIgnoringExpiration(String token, String secretKey) {
        try {
            return parseClaims(token, secretKey).getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (Exception e) {
            throw new CustomException(INVALID_TOKEN);
        }
    }

    public static String parseSubject(String token, String secretKey) {
        try {
            return parseClaims(token, secretKey).getSubject();
        } catch (Exception e) {
            throw new CustomException(INVALID_TOKEN);
        }
    }

    public static boolean isExpired(String token, String secretKey) {
        try {
            Date expiration = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public static Claims parseClaims(String token, String secretKey) throws ExpiredJwtException {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }
}
