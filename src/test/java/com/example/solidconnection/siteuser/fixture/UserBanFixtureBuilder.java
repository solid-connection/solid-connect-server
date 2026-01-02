package com.example.solidconnection.siteuser.fixture;

import com.example.solidconnection.siteuser.domain.UserBan;
import com.example.solidconnection.siteuser.repository.UserBanRepository;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class UserBanFixtureBuilder {

    private final UserBanRepository userBanRepository;

    private Long bannedUserId;
    private ZonedDateTime expiredAt;

    public UserBanFixtureBuilder userBan() {
        return new UserBanFixtureBuilder(userBanRepository);
    }

    public UserBanFixtureBuilder bannedUserId(Long bannedUserId) {
        this.bannedUserId = bannedUserId;
        return this;
    }

    public UserBanFixtureBuilder expiredAt(ZonedDateTime expiredAt) {
        this.expiredAt = expiredAt;
        return this;
    }

    public UserBan create() {
        UserBan userBan = new UserBan(bannedUserId, expiredAt);
        return userBanRepository.save(userBan);
    }
}