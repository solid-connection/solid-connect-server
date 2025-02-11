package com.example.solidconnection.custom.security.mapper;

import com.example.solidconnection.type.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class SecurityRoleMapper {

    private static final String ROLE_PREFIX = "ROLE_";

    private SecurityRoleMapper() {
    }

    public static List<SimpleGrantedAuthority> mapRoleToAuthorities(Role role) {
        return List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role.name()));
    }
}
