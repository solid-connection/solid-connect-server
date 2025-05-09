package com.example.solidconnection.auth.service;

public record RefreshToken(
        Subject subject,
        String token
) {

    RefreshToken(String subject, String token) {
        this(new Subject(subject), token);
    }
}
