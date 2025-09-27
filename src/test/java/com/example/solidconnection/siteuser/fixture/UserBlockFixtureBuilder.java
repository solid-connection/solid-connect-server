package com.example.solidconnection.siteuser.fixture;

import com.example.solidconnection.siteuser.domain.UserBlock;
import com.example.solidconnection.siteuser.repository.UserBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class UserBlockFixtureBuilder {

    private final UserBlockRepository userBlockRepository;

    private long blockerId;
    private long blockedId;

    public UserBlockFixtureBuilder userBlock() {
        return new UserBlockFixtureBuilder(userBlockRepository);
    }

    public UserBlockFixtureBuilder blockerId(long blockerId) {
        this.blockerId = blockerId;
        return this;
    }

    public UserBlockFixtureBuilder blockedId(long blockedId) {
        this.blockedId = blockedId;
        return this;
    }

    public UserBlock create() {
        UserBlock userBlock = new UserBlock(blockerId, blockedId);
        return userBlockRepository.save(userBlock);
    }
}
