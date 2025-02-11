package com.example.solidconnection.custom.security.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

public abstract class JwtAuthentication extends AbstractAuthenticationToken {

    private final String credentials;

    private final Object principal;

    public JwtAuthentication(String token, Object principal) {
        super(principal instanceof UserDetails ?
                ((UserDetails) principal).getAuthorities() :
                Collections.emptyList());
        this.credentials = token;
        this.principal = principal;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    public final String getToken() {
        return (String) getCredentials();
    }
}
