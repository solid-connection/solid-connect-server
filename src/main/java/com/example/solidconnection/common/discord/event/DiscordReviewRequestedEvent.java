package com.example.solidconnection.common.discord.event;

import com.example.solidconnection.common.discord.DiscordNotificationType;

public record DiscordReviewRequestedEvent(
        DiscordNotificationType type,
        long reviewId,
        String applicantInfo
) {

}
