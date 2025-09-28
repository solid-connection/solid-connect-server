package com.example.solidconnection.siteuser.fixture;

import com.example.solidconnection.siteuser.domain.UserBlock;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class UserBlockFixture {

    private final UserBlockFixtureBuilder userBlockFixtureBuilder;

    public UserBlock 유저_차단(long blockerId, long blockedId) {
        return userBlockFixtureBuilder.userBlock()
                .blockerId(blockerId)
                .blockedId(blockedId)
                .create();
    }
}
