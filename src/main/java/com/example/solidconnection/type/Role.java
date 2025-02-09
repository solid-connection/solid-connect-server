package com.example.solidconnection.type;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public enum Role {

    ADMIN,
    MENTOR,
    MENTEE;

    private static final String ROLE_PREFIX = "ROLE_";

    public List<SimpleGrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(ROLE_PREFIX + name()));
    }
}
