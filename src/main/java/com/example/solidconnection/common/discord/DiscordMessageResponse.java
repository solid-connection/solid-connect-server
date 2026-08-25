package com.example.solidconnection.common.discord;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiscordMessageResponse(
        String id,
        @JsonProperty("channel_id") String channelId
) {
}
