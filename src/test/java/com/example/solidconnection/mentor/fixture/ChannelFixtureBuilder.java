package com.example.solidconnection.mentor.fixture;

import com.example.solidconnection.mentor.domain.Channel;
import com.example.solidconnection.mentor.domain.ChannelType;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ChannelFixtureBuilder {

    private final ChannelRepository channelRepository;

    private int sequence;
    private ChannelType type;
    private String url;
    private Mentor mentor;

    public ChannelFixtureBuilder channel() {
        return new ChannelFixtureBuilder(channelRepository);
    }

    public ChannelFixtureBuilder sequence(int sequence) {
        this.sequence = sequence;
        return this;
    }

    public ChannelFixtureBuilder type(ChannelType type) {
        this.type = type;
        return this;
    }

    public ChannelFixtureBuilder url(String url) {
        this.url = url;
        return this;
    }

    public ChannelFixtureBuilder mentor(Mentor mentor) {
        this.mentor = mentor;
        return this;
    }

    public Channel create() {
        Channel channel = new Channel(
                null,
                sequence,
                type,
                url,
                null
        );
        channel.updateMentor(mentor);
        return channelRepository.save(channel);
    }
}