package com.example.solidconnection.security.filter;

public interface BlacklistChecker {

    boolean isTokenBlacklisted(String token);
}
