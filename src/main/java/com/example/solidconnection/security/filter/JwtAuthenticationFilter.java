package com.example.solidconnection.security.filter;

import com.example.solidconnection.security.authentication.JwtAuthentication;
import com.example.solidconnection.security.authentication.SiteUserAuthentication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final AuthorizationHeaderParser authorizationHeaderParser;

    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request,
                                 @NonNull HttpServletResponse response,
                                 @NonNull FilterChain filterChain) throws ServletException, IOException {
        Optional<String> token = authorizationHeaderParser.parseToken(request);
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        JwtAuthentication authToken = createAuthentication(token.get());
        Authentication auth = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private JwtAuthentication createAuthentication(String token) {
        return new SiteUserAuthentication(token);
    }
}
