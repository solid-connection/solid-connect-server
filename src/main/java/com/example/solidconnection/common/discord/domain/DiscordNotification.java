package com.example.solidconnection.common.discord.domain;

import com.example.solidconnection.common.BaseEntity;
import com.example.solidconnection.common.discord.DiscordNotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "discord_notification",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_discord_notification_target",
        columnNames = {"review_type", "review_id"}
    )
)
public class DiscordNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false, length = 32)
    private DiscordNotificationType reviewType;

    @Column(name = "review_id", nullable = false)
    private long reviewId;

    @Column(name = "discord_channel_id", nullable = false, length = 32)
    private String discordChannelId;

    @Column(name = "discord_message_id", nullable = false, length = 32)
    private String discordMessageId;

    private DiscordNotification(
            DiscordNotificationType reviewType,
            long reviewId,
            String discordChannelId,
            String discordMessageId
    ) {
        this.reviewType = reviewType;
        this.reviewId = reviewId;
        this.discordChannelId = discordChannelId;
        this.discordMessageId = discordMessageId;
    }

    public static DiscordNotification of(
            DiscordNotificationType reviewType,
            long reviewId,
            String discordChannelId,
            String discordMessageId
    ) {
        return new DiscordNotification(reviewType, reviewId, discordChannelId, discordMessageId);
    }
}
