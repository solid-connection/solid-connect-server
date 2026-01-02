package com.example.solidconnection.siteuser.fixture;

import com.example.solidconnection.siteuser.domain.UserBan;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class UserBanFixture {

    private final UserBanFixtureBuilder userBanFixtureBuilder;

    public UserBan 차단(long bannedUserId, int days) {
        return userBanFixtureBuilder.userBan()
                .bannedUserId(bannedUserId)
                .expiredAt(ZonedDateTime.now().plusDays(days))
                .create();
    }

    public UserBan 만료된_차단(long bannedUserId) {
        return userBanFixtureBuilder.userBan()
                .bannedUserId(bannedUserId)
                .expiredAt(ZonedDateTime.now().minusDays(1))
                .create();
    }

    public UserBan 수동_차단_해제(long bannedUserId, long adminId) {
        UserBan userBan = userBanFixtureBuilder.userBan()
                .bannedUserId(bannedUserId)
                .expiredAt(ZonedDateTime.now().plusDays(7))
                .create();
        userBan.manuallyUnban(adminId);
        return userBan;
    }
}