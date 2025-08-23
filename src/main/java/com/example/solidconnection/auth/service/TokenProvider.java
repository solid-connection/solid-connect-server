package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.Subject;
import java.util.Map;

public interface TokenProvider {

    String generateToken(Subject subject, long expiration);

    String generateToken(Subject subject, Map<String, String> claims, long expiration);

    Subject parseSubject(String token);

    <T> T parseClaims(String token, String claimName, Class<T> claimType);
}
