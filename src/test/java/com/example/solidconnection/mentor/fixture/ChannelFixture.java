package com.example.solidconnection.mentor.fixture;

import com.example.solidconnection.mentor.domain.Channel;
import com.example.solidconnection.mentor.domain.ChannelType;
import com.example.solidconnection.mentor.domain.Mentor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ChannelFixture {

    private final ChannelFixtureBuilder channelFixtureBuilder;

    public Channel 채널(int sequence, Mentor mentor) {
        return channelFixtureBuilder.channel()
                .sequence(sequence)
                .type(ChannelType.YOUTUBE)
                .url("https://www.youtube.com/channel" + sequence)
                .mentor(mentor)
                .create();
    }
}