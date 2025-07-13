package com.example.solidconnection.security.filter;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationHeaderParser {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final int TOKEN_PREFIX_LENGTH = TOKEN_PREFIX.length();

    public Optional<String> parseToken(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);
        if (isInvalidFormat(token)) {
            return Optional.empty();
        }
        return Optional.of(token.substring(TOKEN_PREFIX_LENGTH));
    }

    private boolean isInvalidFormat(String token) {
        return token == null ||
                token.isBlank() ||
                !token.startsWith(TOKEN_PREFIX) ||
                token.substring(TOKEN_PREFIX_LENGTH).isBlank();
    }
}
