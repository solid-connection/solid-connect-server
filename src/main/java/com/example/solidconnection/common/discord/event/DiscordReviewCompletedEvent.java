package com.example.solidconnection.common.discord.event;

import com.example.solidconnection.common.discord.DiscordNotificationType;

public record DiscordReviewCompletedEvent(
        DiscordNotificationType type,
        long reviewId,
        String applicantInfo,
        String marker
) {

}
