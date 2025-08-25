package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.Subject;
import java.time.Duration;
import java.util.Map;

public interface TokenProvider {

    String generateToken(Subject subject, Duration expiration);

    String generateToken(Subject subject, Map<String, String> claims, Duration expiration);

    Subject parseSubject(String token);

    <T> T parseClaims(String token, String claimName, Class<T> claimType);
}
