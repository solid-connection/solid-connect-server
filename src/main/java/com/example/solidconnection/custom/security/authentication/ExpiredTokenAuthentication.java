package com.example.solidconnection.custom.security.authentication;

// todo: 사용되지 않음, 다른 PR에서 삭제하고 더 효율적인 구조를 고민해봐야 함
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
