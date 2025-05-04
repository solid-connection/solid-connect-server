package com.example.solidconnection.auth.service;

public interface BlacklistChecker {

    boolean isTokenBlacklisted(String token);
}
