package com.example.solidconnection.security.config;

import com.example.solidconnection.security.authentication.TokenAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;

@RequiredArgsConstructor
@Configuration
public class AuthenticationManagerConfig {

    private final TokenAuthenticationProvider tokenAuthenticationProvider;

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(
                tokenAuthenticationProvider
        );
    }
}
