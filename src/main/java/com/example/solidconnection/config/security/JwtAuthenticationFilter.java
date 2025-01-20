package com.example.solidconnection.config.security;

import com.example.solidconnection.custom.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String REISSUE_URI = "/auth/reissue";
    private static final String REISSUE_METHOD = "post";

    private final JwtProperties jwtProperties;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = tokenProvider.parseTokenFromRequest(request);
        if (token == null || isReissueRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String subject = parseSubjectOrElseThrow(token, jwtProperties.secret());
            UserDetails userDetails = new JwtUserDetails(subject);
            Authentication auth = new JwtAuthentication(userDetails, token, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException e) {
            jwtAuthenticationEntryPoint.commence(request, response, e);
        } catch (CustomException e) {
            jwtAuthenticationEntryPoint.customCommence(response, e);
        } catch (Exception e) {
            jwtAuthenticationEntryPoint.generalCommence(response, e);
        }
    }

    private boolean isReissueRequest(HttpServletRequest request) {
        return REISSUE_URI.equals(request.getRequestURI()) && REISSUE_METHOD.equals(request.getMethod());
    }
}
