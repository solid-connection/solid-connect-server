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
