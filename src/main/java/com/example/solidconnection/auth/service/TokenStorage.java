package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.domain.Subject;
import com.example.solidconnection.auth.domain.Token;
import java.util.Optional;

public interface TokenStorage {

    <T extends Token> T saveToken(Subject subject, T token);

    <T extends Token> Optional<String> findToken(Subject subject, Class<T> tokenClass);

    <T extends Token> void deleteToken(Subject subject, Class<T> tokenClass);
}
