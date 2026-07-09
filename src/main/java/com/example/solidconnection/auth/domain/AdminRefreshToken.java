package com.example.solidconnection.auth.domain;

public record AdminRefreshToken(
        String token
) implements Token {

}
