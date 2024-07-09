package com.example.solidconnection.custom.userdetails;

import com.example.solidconnection.siteuser.domain.SiteUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {//todo: principal 을 썼을 때 바로 SiteUser를 반환하게 하면 안되나??

    private final SiteUser siteUser;

    public CustomUserDetails(SiteUser siteUser) {
        this.siteUser = siteUser;
    }

    public String getEmail() {
        return siteUser.getEmail();
    }

    @Override
    public String getUsername() {
        return siteUser.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
