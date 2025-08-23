package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import java.util.Map;

public interface TokenProvider {

    String generateToken(String string, TokenType tokenType);

    String generateToken(String string, Map<String, String> claims, TokenType tokenType);

    String parseSubject(String token);

    <T> T parseClaims(String token, String claimName, Class<T> claimType);
}
