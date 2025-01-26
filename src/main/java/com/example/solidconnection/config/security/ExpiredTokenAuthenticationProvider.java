package com.example.solidconnection.config.security;


import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import static com.example.solidconnection.util.JwtUtils.parseSubjectIgnoringExpiration;

@Component
@RequiredArgsConstructor
public class ExpiredTokenAuthenticationProvider implements AuthenticationProvider {

    private final JwtProperties jwtProperties;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        JwtAuthentication jwtAuth = (JwtAuthentication) auth;
        String token = jwtAuth.getToken();
        String subject = parseSubjectIgnoringExpiration(token, jwtProperties.secret());

        return new ExpiredTokenAuthentication(token, subject);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ExpiredTokenAuthentication.class.isAssignableFrom(authentication);
    }
}
