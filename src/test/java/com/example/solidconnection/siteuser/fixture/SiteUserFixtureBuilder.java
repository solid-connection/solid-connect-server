package com.example.solidconnection.siteuser.fixture;

import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestComponent
@RequiredArgsConstructor
public class SiteUserFixtureBuilder {

    private final SiteUserRepository siteUserRepository;
    private final PasswordEncoder passwordEncoder;

    private String email;
    private AuthType authType;
    private String nickname;
    private String profileImageUrl;
    private Role role;
    private String password;

    public SiteUserFixtureBuilder siteUser() {
        return new SiteUserFixtureBuilder(siteUserRepository, passwordEncoder);
    }

    public SiteUserFixtureBuilder email(String email) {
        this.email = email;
        return this;
    }

    public SiteUserFixtureBuilder authType(AuthType authType) {
        this.authType = authType;
        return this;
    }

    public SiteUserFixtureBuilder nickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public SiteUserFixtureBuilder profileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public SiteUserFixtureBuilder role(Role role) {
        this.role = role;
        return this;
    }

    public SiteUserFixtureBuilder password(String password) {
        this.password = password;
        return this;
    }

    public SiteUser create() {
        SiteUser siteUser = new SiteUser(
                email,
                nickname,
                profileImageUrl,
                PreparationStatus.CONSIDERING,
                role,
                authType,
                passwordEncoder.encode(password)
        );
        return siteUserRepository.save(siteUser);
    }
}
