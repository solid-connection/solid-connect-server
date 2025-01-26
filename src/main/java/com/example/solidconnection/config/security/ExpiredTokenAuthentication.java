package com.example.solidconnection.config.security;

public class ExpiredTokenAuthentication extends JwtAuthentication {

    public ExpiredTokenAuthentication(String token) {
        super(token, null);
        setAuthenticated(false);
    }

    public ExpiredTokenAuthentication(String token, String subject) {
        super(token, subject);
        setAuthenticated(false);
    }

    public String getSubject() {
        return (String) getPrincipal();
    }
}
