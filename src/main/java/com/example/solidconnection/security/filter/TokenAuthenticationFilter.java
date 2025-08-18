package com.example.solidconnection.security.filter;

import com.example.solidconnection.security.authentication.TokenAuthentication;
import com.example.solidconnection.security.infrastructure.AuthorizationHeaderParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final AuthorizationHeaderParser authorizationHeaderParser;

    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request,
                                 @NonNull HttpServletResponse response,
                                 @NonNull FilterChain filterChain) throws ServletException, IOException {
        Optional<String> token = resolveToken(request);
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        TokenAuthentication authToken = new TokenAuthentication(token.get());
        Authentication auth = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private Optional<String> resolveToken(HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/connect")) {
            return Optional.ofNullable(request.getParameter("token"));
        }
        return authorizationHeaderParser.parseToken(request);
    }
}
