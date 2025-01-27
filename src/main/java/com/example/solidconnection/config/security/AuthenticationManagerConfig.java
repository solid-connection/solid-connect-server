package com.example.solidconnection.config.security;

import com.example.solidconnection.custom.security.provider.ExpiredTokenAuthenticationProvider;
import com.example.solidconnection.custom.security.provider.SiteUserAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;

@RequiredArgsConstructor
@Configuration
public class AuthenticationManagerConfig {

    private final SiteUserAuthenticationProvider siteUserAuthenticationProvider;
    private final ExpiredTokenAuthenticationProvider expiredTokenAuthenticationProvider;

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(
                siteUserAuthenticationProvider,
                expiredTokenAuthenticationProvider
        );
    }
}
