package com.example.solidconnection.security.filter;

import com.example.solidconnection.common.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import static com.example.solidconnection.common.exception.ErrorCode.USER_ALREADY_SIGN_OUT;

@Component
@RequiredArgsConstructor
public class SignOutCheckFilter extends OncePerRequestFilter {

    private final AuthorizationHeaderParser authorizationHeaderParser;
    private final BlacklistChecker blacklistChecker;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        Optional<String> token = authorizationHeaderParser.parseToken(request);
        if (token.isPresent() && hasSignedOut(token.get())) {
            throw new CustomException(USER_ALREADY_SIGN_OUT);
        }
        filterChain.doFilter(request, response);
    }

    private boolean hasSignedOut(String accessToken) {
        return blacklistChecker.isTokenBlacklisted(accessToken);
    }
}
