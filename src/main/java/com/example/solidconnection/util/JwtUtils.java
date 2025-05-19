package com.example.solidconnection.util;

import com.example.solidconnection.common.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import static com.example.solidconnection.common.exception.ErrorCode.INVALID_TOKEN;

@Component
public class JwtUtils {

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
