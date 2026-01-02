package com.example.solidconnection.siteuser.fixture;

import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class SiteUserFixture {

    private final SiteUserFixtureBuilder siteUserFixtureBuilder;

    public SiteUser 사용자() {
        return siteUserFixtureBuilder.siteUser()
                .email("test@example.com")
                .authType(AuthType.EMAIL)
                .nickname("사용자")
                .profileImageUrl("profileImageUrl")
                .role(Role.MENTEE)
                .password("password123")
                .userStatus(UserStatus.ACTIVE)
                .create();
    }

    public SiteUser 사용자(int index, String nickname) {
        return siteUserFixtureBuilder.siteUser()
                .email("test" + index + "@example.com")
                .authType(AuthType.EMAIL)
                .nickname(nickname)
                .profileImageUrl("profileImageUrl")
                .role(Role.MENTEE)
                .password("password123")
                .userStatus(UserStatus.ACTIVE)
                .create();
    }

    public SiteUser 사용자(String email, AuthType authType) {
        return siteUserFixtureBuilder.siteUser()
                .email(email)
                .authType(authType)
                .nickname("사용자")
                .profileImageUrl("profileImageUrl")
                .role(Role.MENTEE)
                .password("password123")
                .userStatus(UserStatus.ACTIVE)
                .create();
    }

    public SiteUser 사용자(String email, String password) {
        return siteUserFixtureBuilder.siteUser()
                .email(email)
                .authType(AuthType.EMAIL)
                .nickname("사용자")
                .profileImageUrl("profileImageUrl")
                .role(Role.MENTEE)
                .password(password)
                .userStatus(UserStatus.ACTIVE)
                .create();
    }

    public SiteUser 멘토(int index, String nickname) {
        return siteUserFixtureBuilder.siteUser()
                .email("mentor" + index + "@example.com")
                .authType(AuthType.EMAIL)
                .nickname(nickname)
                .profileImageUrl("profileImageUrl")
                .role(Role.MENTOR)
                .password("mentor123")
                .userStatus(UserStatus.ACTIVE)
                .create();
    }

    public SiteUser 관리자() {
        return siteUserFixtureBuilder.siteUser()
                .email("admin@example.com")
                .authType(AuthType.EMAIL)
                .nickname("관리자")
                .profileImageUrl("profileImageUrl")
                .role(Role.ADMIN)
                .password("admin123")
                .userStatus(UserStatus.ACTIVE)
                .create();
    }

    public SiteUser 차단된_사용자(String nickname) {
        return siteUserFixtureBuilder.siteUser()
                .email("banned@example.com")
                .authType(AuthType.EMAIL)
                .nickname(nickname)
                .profileImageUrl("profileImageUrl")
                .role(Role.MENTEE)
                .password("banned123")
                .userStatus(UserStatus.BANNED)
                .create();
    }
}
