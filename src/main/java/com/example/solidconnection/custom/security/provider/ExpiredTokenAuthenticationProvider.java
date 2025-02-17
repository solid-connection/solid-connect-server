package com.example.solidconnection.custom.security.provider;


import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.custom.security.authentication.ExpiredTokenAuthentication;
import com.example.solidconnection.custom.security.authentication.JwtAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import static com.example.solidconnection.util.JwtUtils.parseSubjectIgnoringExpiration;

// todo: 사용되지 않음, 다른 PR에서 삭제하고 더 효율적인 구조를 고민해봐야 함
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
