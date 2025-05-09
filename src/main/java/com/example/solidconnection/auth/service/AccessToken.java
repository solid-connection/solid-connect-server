package com.example.solidconnection.auth.service;

public record AccessToken(
        Subject subject,
        String token
) {

    public AccessToken(String subject, String token) {
        this(new Subject(subject), token);
    }
}
