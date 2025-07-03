package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.mentor.domain.Channel;
import com.example.solidconnection.mentor.domain.ChannelType;

public record ChannelResponse(
        ChannelType type,
        String url
) {

    public static ChannelResponse from(Channel channel) {
        return new ChannelResponse(
                channel.getType(),
                channel.getUrl()
        );
    }
}
