package com.example.solidconnection.siteuser.fixture;

import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class SiteUserFixtureBuilder {

    private final SiteUserRepository siteUserRepository;

    private String email;
    private AuthType authType;
    private String nickname;
    private String profileImageUrl;
    private Role role;
    private String password;

    /**
     * Creates a new instance of the builder with the same repository for fresh configuration.
     *
     * @return a new SiteUserFixtureBuilder instance
     */
    public SiteUserFixtureBuilder siteUser() {
        return new SiteUserFixtureBuilder(siteUserRepository);
    }

    /**
     * Sets the email address for the SiteUser being built.
     *
     * @param email the email address to assign
     * @return this builder instance for method chaining
     */
    public SiteUserFixtureBuilder email(String email) {
        this.email = email;
        return this;
    }

    /**
     * Sets the authentication type for the user being built.
     *
     * @param authType the authentication type to assign
     * @return this builder instance for method chaining
     */
    public SiteUserFixtureBuilder authType(AuthType authType) {
        this.authType = authType;
        return this;
    }

    /**
     * Sets the nickname for the SiteUser being built.
     *
     * @param nickname the nickname to assign
     * @return this builder instance for method chaining
     */
    public SiteUserFixtureBuilder nickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    /**
     * Sets the profile image URL for the SiteUser being built.
     *
     * @param profileImageUrl the URL of the user's profile image
     * @return this builder instance for method chaining
     */
    public SiteUserFixtureBuilder profileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    /**
     * Sets the role for the SiteUser being built.
     *
     * @param role the user role to assign
     * @return this builder instance for method chaining
     */
    public SiteUserFixtureBuilder role(Role role) {
        this.role = role;
        return this;
    }

    /**
     * Sets the password for the SiteUser being built.
     *
     * @param password the password to assign
     * @return this builder instance for method chaining
     */
    public SiteUserFixtureBuilder password(String password) {
        this.password = password;
        return this;
    }

    /**
     * Builds and persists a new SiteUser entity using the configured attributes.
     *
     * @return the persisted SiteUser instance
     */
    public SiteUser create() {
        SiteUser siteUser = new SiteUser(
                email,
                nickname,
                profileImageUrl,
                PreparationStatus.CONSIDERING,
                role,
                authType,
                password
        );
        return siteUserRepository.save(siteUser);
    }
}
