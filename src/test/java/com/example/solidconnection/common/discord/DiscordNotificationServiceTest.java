package com.example.solidconnection.common.discord;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.solidconnection.common.discord.service.DiscordNotificationService;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
class DiscordNotificationServiceTest {

    @Autowired
    private DiscordNotificationService discordNotificationService;

    @Test
    void 검수별_디스코드_메시지_식별자를_저장한다() {
        // given
        DiscordNotificationType reviewType = DiscordNotificationType.GPA_SCORE;
        long reviewId = 1L;

        // when
        discordNotificationService.save(reviewType, reviewId, "channel-id", "message-id");

        // then
        assertThat(discordNotificationService.findByReviewTypeAndReviewId(reviewType, reviewId))
                .hasValueSatisfying(notification -> {
                    assertThat(notification.getDiscordChannelId()).isEqualTo("channel-id");
                    assertThat(notification.getDiscordMessageId()).isEqualTo("message-id");
                });
    }
}
