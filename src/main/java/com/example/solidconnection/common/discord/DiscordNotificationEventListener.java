package com.example.solidconnection.common.discord;

import com.example.solidconnection.common.discord.event.DiscordReportCreatedEvent;
import com.example.solidconnection.common.discord.event.DiscordReviewCompletedEvent;
import com.example.solidconnection.common.discord.event.DiscordReviewRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class DiscordNotificationEventListener {

    private final DiscordNotifier discordNotifier;

    @Async("asyncExecutor")
    @TransactionalEventListener
    public void handleReviewRequested(DiscordReviewRequestedEvent event) {
        try {
            discordNotifier.notify(event.type(), event.reviewId(), event.applicantInfo());
        } catch (Exception e) {
            log.error("검수 요청 알림을 전송하지 못했습니다. reviewType={}, reviewId={}, exception={}",
                    event.type(), event.reviewId(), e.getClass().getSimpleName());
        }
    }

    @Async("asyncExecutor")
    @TransactionalEventListener
    public void handleReviewCompleted(DiscordReviewCompletedEvent event) {
        try {
            discordNotifier.markReviewResult(event.type(), event.reviewId(), event.applicantInfo(), event.marker());
        } catch (Exception e) {
            log.error("검수 결과를 표시하지 못했습니다. reviewType={}, reviewId={}, exception={}",
                    event.type(), event.reviewId(), e.getClass().getSimpleName());
        }
    }

    @Async("asyncExecutor")
    @TransactionalEventListener
    public void handleReportCreated(DiscordReportCreatedEvent event) {
        discordNotifier.notify(DiscordNotificationType.REPORT, "신고자 ID: " + event.reporterId());
    }
}
