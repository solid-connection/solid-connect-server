package com.example.solidconnection.siteuser.fixture;

import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestComponent
@RequiredArgsConstructor
public class SiteUserFixture {

    private final SiteUserFixtureBuilder siteUserFixtureBuilder;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a default test user with preset email, nickname, role, and password.
     *
     * @return a SiteUser instance representing a standard test user
     */
    public SiteUser 테스트_유저() {
        return siteUserFixtureBuilder.siteUser()
                .email("test@example.com")
                .authType(AuthType.EMAIL)
                .nickname("테스트유저")
                .profileImageUrl("profileImageUrl")
                .role(Role.MENTEE)
                .password("password123")
                .create();
    }

    /**
     * Creates a test user with a unique email based on the given index and the specified nickname.
     *
     * @param index the numeric value to include in the user's email address for uniqueness
     * @param nickname the nickname to assign to the user
     * @return a new SiteUser instance with the specified nickname and a unique email
     */
    public SiteUser 테스트_유저(int index, String nickname) {
        return siteUserFixtureBuilder.siteUser()
                .email("test" + index + " @example.com")
                .authType(AuthType.EMAIL)
                .nickname(nickname)
                .profileImageUrl("profileImageUrl")
                .role(Role.MENTEE)
                .password("password123")
                .create();
    }

    /**
     * Creates a test user with the specified email and authentication type.
     *
     * @param email the email address for the test user
     * @param authType the authentication type for the test user
     * @return a SiteUser instance with the given email and auth type, default nickname, profile image URL, mentee role, and password
     */
    public SiteUser 테스트_유저(String email, AuthType authType) {
        return siteUserFixtureBuilder.siteUser()
                .email(email)
                .authType(authType)
                .nickname("테스트유저")
                .profileImageUrl("profileImageUrl")
                .role(Role.MENTEE)
                .password("password123")
                .create();
    }

    /**
     * Creates a test user with the specified email and password, encoding the password.
     *
     * @param email the email address for the test user
     * @param password the raw password to be encoded for the test user
     * @return a SiteUser instance with the given email and encoded password, default nickname, profile image URL, and mentee role
     */
    public SiteUser 테스트_유저(String email, String password) {
        return siteUserFixtureBuilder.siteUser()
                .email(email)
                .authType(AuthType.EMAIL)
                .nickname("테스트유저")
                .profileImageUrl("profileImageUrl")
                .role(Role.MENTEE)
                .password(passwordEncoder.encode(password))
                .create();
    }

    /**
     * Creates a test user with a duplicated nickname for testing nickname collision scenarios.
     *
     * @return a SiteUser instance with a fixed email, nickname "중복닉네임", and default attributes
     */
    public SiteUser 중복_닉네임_테스트_유저() {
        return siteUserFixtureBuilder.siteUser()
                .email("duplicated@example.com")
                .authType(AuthType.EMAIL)
                .nickname("중복닉네임")
                .profileImageUrl("profileImageUrl")
                .role(Role.MENTEE)
                .password("password123")
                .create();
    }

    /**
     * Creates a test user with a custom profile image URL and password.
     *
     * @return a SiteUser instance with email "customProfile@example.com", nickname "커스텀프로필", role MENTEE, authentication type EMAIL, profile image URL "profile/profileImageUrl", and password "customPassword123"
     */
    public SiteUser 커스텀_프로필_테스트_유저() {
        return siteUserFixtureBuilder.siteUser()
                .email("customProfile@example.com")
                .authType(AuthType.EMAIL)
                .nickname("커스텀프로필")
                .profileImageUrl("profile/profileImageUrl")
                .role(Role.MENTEE)
                .password("customPassword123")
                .create();
    }

    /**
     * Creates a test admin user with predefined email, nickname, profile image URL, and password.
     *
     * @return a SiteUser instance representing an admin user for testing purposes
     */
    public SiteUser 테스트_어드민() {
        return siteUserFixtureBuilder.siteUser()
                .email("admin@example.com")
                .authType(AuthType.EMAIL)
                .nickname("테스트어드민")
                .profileImageUrl("profileImageUrl")
                .role(Role.ADMIN)
                .password("admin123")
                .create();
    }
}
