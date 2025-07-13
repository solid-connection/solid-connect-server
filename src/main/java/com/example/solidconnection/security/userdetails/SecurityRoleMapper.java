package com.example.solidconnection.security.userdetails;

import com.example.solidconnection.siteuser.domain.Role;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class SecurityRoleMapper {

    private static final String ROLE_PREFIX = "ROLE_";

    private SecurityRoleMapper() {
    }

    public static List<SimpleGrantedAuthority> mapRoleToAuthorities(Role role) {
        return List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role.name()));
    }
}
