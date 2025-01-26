package com.example.solidconnection.config.security;


import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import static com.example.solidconnection.util.JwtUtils.parseSubject;

@Component
@RequiredArgsConstructor
public class SiteUserAuthenticationProvider implements AuthenticationProvider {

    private final JwtProperties jwtProperties;
    private final SiteUserDetailsService siteUserDetailsService;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        JwtAuthentication jwtAuth = (JwtAuthentication) auth;
        String token = jwtAuth.getToken();

        String username = parseSubject(token, jwtProperties.secret());
        SiteUserDetails userDetails = (SiteUserDetails) siteUserDetailsService.loadUserByUsername(username);
        return new SiteUserAuthentication(token, userDetails);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SiteUserAuthentication.class.isAssignableFrom(authentication);
    }
}
