package com.example.solidconnection.siteuser.fixture;

import com.example.solidconnection.siteuser.domain.UserBan;
import com.example.solidconnection.siteuser.domain.UserBanDuration;

import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class UserBanFixture {

    private final UserBanFixtureBuilder userBanFixtureBuilder;

    public UserBan 만료된_차단(long bannedUserId) {
        return userBanFixtureBuilder.userBan()
                .bannedUserId(bannedUserId)
                .duration(UserBanDuration.ONE_DAY)
                .expiredAt(ZonedDateTime.now().minusDays(1))
                .create();
    }

    public UserBan 수동_차단_해제(long bannedUserId, long adminId) {
        UserBan userBan = userBanFixtureBuilder.userBan()
                .bannedUserId(bannedUserId)
                .duration(UserBanDuration.SEVEN_DAYS)
                .expiredAt(ZonedDateTime.now().plusDays(7))
                .create();
        userBan.manuallyUnban(adminId);
        return userBan;
    }
}
