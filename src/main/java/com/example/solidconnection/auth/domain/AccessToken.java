package com.example.solidconnection.auth.domain;

import com.example.solidconnection.siteuser.domain.Role;

public record AccessToken(
        Subject subject,
        Role role,
        String token
) {

    public AccessToken(String subject, Role role, String token) {
        this(new Subject(subject), role, token);
    }
}
