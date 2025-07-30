package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import io.jsonwebtoken.Claims;
import java.util.Map;

public interface TokenProvider {

    String generateToken(String string, TokenType tokenType);

    String generateToken(String string, Map<String, String> claims, TokenType tokenType);

    String saveToken(String token, TokenType tokenType);

    String parseSubject(String token);

    Claims parseClaims(String token);
}
