package com.example.solidconnection.common.discord;

import static org.awaitility.Awaitility.await;
import static org.mockito.BDDMockito.then;

import com.example.solidconnection.common.discord.event.DiscordReviewRequestedEvent;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

@TestContainerSpringBootTest
@DisplayName("디스코드 알림 이벤트 리스너 테스트")
class DiscordNotificationEventListenerTest {

    private static final DiscordReviewRequestedEvent EVENT =
            new DiscordReviewRequestedEvent(DiscordNotificationType.GPA_SCORE, 1L, "홍길동");

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoBean
    private DiscordNotifier discordNotifier;

    @Test
    void 트랜잭션이_커밋되면_알림을_전송한다() {
        // when
        transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(EVENT));

        // then
        await().atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> then(discordNotifier).should()
                        .notify(EVENT.type(), EVENT.reviewId(), EVENT.applicantInfo()));
    }

    @Test
    void 트랜잭션이_롤백되면_알림을_전송하지_않는다() {
        // when
        transactionTemplate.executeWithoutResult(status -> {
            eventPublisher.publishEvent(EVENT);
            status.setRollbackOnly();
        });

        // then
        await().during(Duration.ofMillis(300))
                .atMost(Duration.ofSeconds(1))
                .untilAsserted(() -> then(discordNotifier).shouldHaveNoInteractions());
    }
}
