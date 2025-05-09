package com.example.solidconnection.custom.security.filter;

import com.example.solidconnection.auth.service.BlacklistChecker;
import com.example.solidconnection.custom.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.example.solidconnection.custom.exception.ErrorCode.USER_ALREADY_SIGN_OUT;
import static com.example.solidconnection.util.JwtUtils.parseTokenFromRequest;

@Component
@RequiredArgsConstructor
public class SignOutCheckFilter extends OncePerRequestFilter {

    private final BlacklistChecker tokenBlacklistChecker;

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
        return tokenBlacklistChecker.isTokenBlacklisted(accessToken);
    }
}
