package com.example.solidconnection.security.authentication;

import java.util.Collections;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

public abstract class JwtAuthentication extends AbstractAuthenticationToken {

    private final Object principal; // 인증 주체

    private final String credentials; // 증명 수단

    public JwtAuthentication(String token, Object principal) {
        super(principal instanceof UserDetails ?
                      ((UserDetails) principal).getAuthorities() :
                      Collections.emptyList());
        this.principal = principal;
        this.credentials = token;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    public final String getToken() {
        return (String) getCredentials();
    }
}
