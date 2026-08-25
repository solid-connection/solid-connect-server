package com.example.solidconnection.common.discord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.solidconnection.common.discord.domain.DiscordNotification;
import com.example.solidconnection.common.discord.repository.DiscordNotificationRepository;
import com.example.solidconnection.common.discord.service.DiscordNotificationService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiscordNotificationServiceTest {

    @Mock
    private DiscordNotificationRepository discordNotificationRepository;

    @InjectMocks
    private DiscordNotificationService discordNotificationService;

    @Test
    void 검수별_디스코드_메시지_식별자를_저장한다() {
        // given
        DiscordNotificationType reviewType = DiscordNotificationType.GPA_SCORE;
        long reviewId = 1L;
        ArgumentCaptor<DiscordNotification> notificationCaptor = ArgumentCaptor.forClass(DiscordNotification.class);

        // when
        discordNotificationService.save(reviewType, reviewId, "channel-id", "message-id");

        // then
        then(discordNotificationRepository).should().save(notificationCaptor.capture());
        DiscordNotification notification = notificationCaptor.getValue();
        assertThat(notification.getReviewType()).isEqualTo(reviewType);
        assertThat(notification.getReviewId()).isEqualTo(reviewId);
        assertThat(notification.getDiscordChannelId()).isEqualTo("channel-id");
        assertThat(notification.getDiscordMessageId()).isEqualTo("message-id");
    }

    @Test
    void 검수별_디스코드_메시지를_조회한다() {
        // given
        DiscordNotification notification = DiscordNotification.of(
                DiscordNotificationType.GPA_SCORE,
                1L,
                "channel-id",
                "message-id"
        );
        given(discordNotificationRepository.findByReviewTypeAndReviewId(
                DiscordNotificationType.GPA_SCORE,
                1L
        )).willReturn(Optional.of(notification));

        // when
        Optional<DiscordNotification> result = discordNotificationService.findByReviewTypeAndReviewId(
                DiscordNotificationType.GPA_SCORE,
                1L
        );

        // then
        assertThat(result).contains(notification);
    }
}
