package com.example.solidconnection.chat.config;

import java.security.Principal;

public record SiteUserPrincipal(Long id, String email) implements Principal {

    @Override
    public String getName() {
        return this.email;
    }
}
