package com.example.solidconnection.common.discord.service;

import com.example.solidconnection.common.discord.DiscordNotificationType;
import com.example.solidconnection.common.discord.domain.DiscordNotification;
import com.example.solidconnection.common.discord.repository.DiscordNotificationRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiscordNotificationService {

    private final DiscordNotificationRepository discordNotificationRepository;

    @Transactional
    public void save(
            DiscordNotificationType reviewType,
            long reviewId,
            String channelId,
            String messageId
    ) {
        discordNotificationRepository.save(DiscordNotification.of(reviewType, reviewId, channelId, messageId));
    }

    @Transactional(readOnly = true)
    public Optional<DiscordNotification> findByReviewTypeAndReviewId(
            DiscordNotificationType reviewType,
            long reviewId
    ) {
        return discordNotificationRepository.findByReviewTypeAndReviewId(reviewType, reviewId);
    }
}
