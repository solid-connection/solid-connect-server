package com.example.solidconnection.security.provider;

import com.example.solidconnection.auth.service.TokenProvider;
import com.example.solidconnection.security.authentication.JwtAuthentication;
import com.example.solidconnection.security.authentication.SiteUserAuthentication;
import com.example.solidconnection.security.config.JwtProperties;
import com.example.solidconnection.security.userdetails.SiteUserDetails;
import com.example.solidconnection.security.userdetails.SiteUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SiteUserAuthenticationProvider implements AuthenticationProvider {

    private final JwtProperties jwtProperties;
    private final SiteUserDetailsService siteUserDetailsService;
    private final TokenProvider tokenProvider;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        JwtAuthentication jwtAuth = (JwtAuthentication) auth;
        String token = jwtAuth.getToken();

        String username = tokenProvider.parseSubject(token, jwtProperties.secret());
        SiteUserDetails userDetails = (SiteUserDetails) siteUserDetailsService.loadUserByUsername(username);
        return new SiteUserAuthentication(token, userDetails);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SiteUserAuthentication.class.isAssignableFrom(authentication);
    }
}
