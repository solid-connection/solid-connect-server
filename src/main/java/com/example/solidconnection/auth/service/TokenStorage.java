package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.TokenType;
import java.util.Optional;

public interface TokenStorage {

    String saveToken(String token, TokenType tokenType);

    Optional<String> findToken(String subject, TokenType tokenType);

    void deleteToken(String subject, TokenType tokenType);
}
