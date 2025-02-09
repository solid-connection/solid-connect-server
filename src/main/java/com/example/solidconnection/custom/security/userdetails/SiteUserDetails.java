package com.example.solidconnection.custom.security.userdetails;

import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class SiteUserDetails implements UserDetails {

    // userDetails 에서 userName 은 사용자 식별자를 의미함
    private final String userName;

    @Getter
    private final SiteUser siteUser;

    public SiteUserDetails(SiteUser siteUser) {
        this.siteUser = siteUser;
        this.userName = String.valueOf(siteUser.getId());
    }

    @Override
    public String getUsername() {
        return this.userName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return siteUser.getRole().getAuthorities();
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
