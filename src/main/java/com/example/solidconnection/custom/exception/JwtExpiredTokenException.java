package com.example.solidconnection.custom.exception;

import org.springframework.security.core.AuthenticationException;

public class JwtExpiredTokenException extends AuthenticationException {

    public JwtExpiredTokenException(String msg) {
        super(msg);
    }
}
