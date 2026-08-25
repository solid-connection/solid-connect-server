package com.example.solidconnection.common.discord.repository;

import com.example.solidconnection.common.discord.DiscordNotificationType;
import com.example.solidconnection.common.discord.domain.DiscordNotification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscordNotificationRepository extends JpaRepository<DiscordNotification, Long> {

    Optional<DiscordNotification> findByReviewTypeAndReviewId(
            DiscordNotificationType reviewType,
            long reviewId
    );
}
