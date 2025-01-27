package com.example.solidconnection.config.security;

import com.example.solidconnection.custom.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.example.solidconnection.auth.domain.TokenType.BLACKLIST;
import static com.example.solidconnection.custom.exception.ErrorCode.USER_ALREADY_SIGN_OUT;
import static com.example.solidconnection.util.JwtUtils.parseTokenFromRequest;

@Component
@RequiredArgsConstructor
public class SignOutCheckFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = parseTokenFromRequest(request);
        if (token != null && hasSignedOut(token)) {
            throw new CustomException(USER_ALREADY_SIGN_OUT);
        }
        filterChain.doFilter(request, response);
    }

    private boolean hasSignedOut(String accessToken) {
        String blacklistKey = BLACKLIST.addPrefixToSubject(accessToken);
        return redisTemplate.opsForValue().get(blacklistKey) != null;
    }
}
